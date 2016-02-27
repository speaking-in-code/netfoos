package net.speakingincode.foos.scrape;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.FluentWait;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public class IfpScraper implements Worker<String, Integer> {
  private static final String POINTS_URL =
      "http://ifp.everguide.com/commander/tour/public/PlayerProfile.aspx";
  private static final int INIT_LOAD_DELAY_SECONDS = 10;
  private static final int LOAD_FINISH_MILLIS = 2000;
  private static final int POLL_MILLIS = 100;
  private static final Logger logger = Logger.getLogger(IfpScraper.class.getName());
  private static final Splitter nameSplitter = Splitter.on(',').trimResults();
  private static final Splitter lineSplitter = Splitter.on('\n').trimResults();
  private static final Pattern pointsPattern =
      Pattern.compile("\\d+/(\\d+) Singles/Doubles Points");
  private static final By DROP_DOWN = By.id("R_DropDown");
  private static final By FIRST_ITEM = By.id("R_c0");
  private static final By SECOND_ITEM = By.id("R_c1");
  private static Factory factory = new Factory();
  
  private static class Factory implements Worker.Factory<String, Integer> {
    @Override
    public Worker<String, Integer> newWorker() {
      return new IfpScraper();
    }
    
  }
  public static Worker.Factory<String, Integer> factory() {
    return factory;
  }
  
  private final WebDriver driver;
  private final FluentWait<WebDriver> wait;

  @Override
  public void shutdown() {
    driver.close();
  }
  
  public IfpScraper() {
    driver = new ChromeDriver();
    wait = new FluentWait<WebDriver>(driver)
        .withTimeout(INIT_LOAD_DELAY_SECONDS, TimeUnit.SECONDS)
        .pollingEvery(POLL_MILLIS, TimeUnit.MILLISECONDS)
        .ignoring(NoSuchElementException.class);
  }
  
  @Override
  public Integer convert(String playerName) {
    return scrapePoints(playerName);
  }
  
  public int scrapePoints(String playerName) {
    playerName = toIfpName(playerName);
    logger.info("Loading IFP Points for " + playerName);
    driver.get(POINTS_URL);
    if (!driver.getTitle().contains("Player Profile & Registration")) {
      return -1;
    }
    WebElement input = driver.findElement(By.id("R_Input"));
    if (input == null) {
      logger.info("Could not find R_Input: " + driver.getPageSource());
      return -1;
    }
    input.sendKeys(playerName);
    try {
      WebElement player = waitForDropDownToStabilize();
      player.click();
      WebElement rating = waitForLoad(By.id("lblRating"));
      int points = parsePoints(rating.getText());
      logger.info(playerName + ": " + points);
      return points;
    } catch (AmbiguousPlayerNameException e) {
      logger.info("Ambiguous: " + playerName + ": 0");
      return 0;
    } catch (NoPlayerMatchException e) {
      logger.info("No match: " + playerName + ": 0");
      return 0;
    }
  }
  
  private WebElement waitForLoad(final By toFind) {
    return wait.until(new Function<WebDriver, WebElement>() {
      public WebElement apply(WebDriver driver) {
        return driver.findElement(toFind);
      }
    });
  }
  
  /**
   * The drop down menu refreshes dynamically as more data arrives. We wait for the first
   * item to appear, then wait for there to be no second item, then return the first item
   * in the list.
   * 
   * @throws AmbiguousPlayerNameException 
   * @throws NoPlayerMatchException 
   */
  private WebElement waitForDropDownToStabilize()
      throws AmbiguousPlayerNameException, NoPlayerMatchException {
    wait.until(new Predicate<WebDriver>() {
      @Override
      public boolean apply(WebDriver input) {
        WebElement dropDown = input.findElement(DROP_DOWN);
        if (dropDown == null) {
          return false;
        }
        return !dropDown.getText().equals("Loading...");
      }   
    });
    // No idea what the page is doing, but a delay here is required. Otherwise the player
    // element vanishes and then gets regenerated.
    waitForMillis(LOAD_FINISH_MILLIS);
    List<WebElement> firstPlayer = driver.findElements(FIRST_ITEM);
    if (firstPlayer.isEmpty()) {
      throw new NoPlayerMatchException();
    }
    String firstPlayerName = firstPlayer.get(0).getText();
    List<WebElement> secondPlayer = driver.findElements(SECOND_ITEM);
    if (!secondPlayer.isEmpty()) {
      throw new AmbiguousPlayerNameException(ImmutableList.of(
          firstPlayerName, secondPlayer.get(0).getText()));
    }
    return driver.findElement(FIRST_ITEM);
  }
  
  private void waitForMillis(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
    }
  }
  
  private String toIfpName(String playerName) {
    List<String> names = nameSplitter.splitToList(playerName);
    // This is amazingly picky, and is basically wrong no matter what.
    // For Ben Kempner, there must be two spaces between the names in order to find points.
    // For Wes Hunt, there must be only one space between the names.
    // I have no idea why the web site works that way.
    // Short of trying both for everyone, I don't know how this could be made to work.
    return names.get(1) + " " + names.get(0);
  }
  
  /**
   * Format is like this for men:
   * 
   * 6656/6716 Singles/Doubles Points
   * MASTER
   * 
   * and like this for women:
   * 
   * 1300/1500 Singles/Doubles Points
   * 3430/3654 Women's Singles/Doubles Points
   * PRO
   */
  @VisibleForTesting
  static int parsePoints(String record) {
    List<String> lines = lineSplitter.splitToList(record);
    Matcher m = pointsPattern.matcher(lines.get(0));
    m.matches();
    return Integer.parseInt(m.group(1));
  }
}
