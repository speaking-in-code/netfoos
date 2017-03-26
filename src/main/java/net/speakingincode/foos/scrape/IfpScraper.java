package net.speakingincode.foos.scrape;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
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
import com.google.common.collect.Lists;

public class IfpScraper implements Worker<String, Integer> {
  private static final String POINTS_URL =
      "http://ifp.everguide.com/commander/tour/public/PlayerProfile.aspx";
  private static final int INIT_LOAD_DELAY_SECONDS = 10;
  private static final int LOAD_FINISH_MILLIS = 2000;
  private static final int POLL_MILLIS = 100;
  private static final Logger logger = Logger.getLogger(IfpScraper.class.getName());
  private static final Splitter lineSplitter = Splitter.on('\n').trimResults();
  private static final Pattern pointsPattern =
      Pattern.compile("\\d+/(\\d+) Singles/Doubles Points");
  private static final By DROP_DOWN = By.id("R_DropDown");
  private static Factory factory = new Factory();
  
  // Matches stuff like this:
  //   Paul Richards
  //   Paul Richards (CA)
  // If the longer version is there, only the first part is returned.
  private static final Pattern WITHOUT_STATE = Pattern.compile("([^(]*)( \\(.*\\))?");
  
  private static class Factory implements Worker.Factory<String, Integer> {
    @Override
    public Worker<String, Integer> newWorker() {
      return new IfpScraper();
    }
    
  }
  public static Worker.Factory<String, Integer> factory() {
    return factory;
  }
  
  private WebDriver driver;
  private final FluentWait<WebDriver> wait;

  @Override
  public void shutdown() {
    if (driver != null) {
      driver.close();
      driver = null;
    }
  }
  
  public IfpScraper() {
    driver = new ChromeDriver();
    // Try to avoid leaking a chrome process.
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        shutdown();
      }
    });
    wait = new FluentWait<WebDriver>(driver)
        .withTimeout(INIT_LOAD_DELAY_SECONDS, TimeUnit.SECONDS)
        .pollingEvery(POLL_MILLIS, TimeUnit.MILLISECONDS)
        .ignoring(NoSuchElementException.class);
  }
  
  @Override
  public Integer convert(String playerName) {
    return scrapePoints(playerName);
  }
  
  @VisibleForTesting
  static MatchResult matchFullText(String fullText) {
    Matcher m = WITHOUT_STATE.matcher(fullText);
    if (!m.matches()) {
      return null;
    }
    return m.toMatchResult();
  }
  
  public int scrapePoints(String fullText) {
    String playerName = fullText;
    MatchResult match = matchFullText(fullText);
    if (match == null) {
      throw new IllegalStateException("No match for " + fullText);
    }
    playerName = match.group(1);
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
      WebElement player = waitForDropDownToStabilize(fullText);
      player.click();
      WebElement rating = waitForLoad(By.id("lblRating"));
      int points = parsePoints(rating.getText());
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
  private WebElement waitForDropDownToStabilize(String fullMatch)
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
    
    // Players that match are listed as R_c0, R_c1, ...
    List<WebElement> candidates = Lists.newArrayList();
    for (int index = 0; ; ++index) {
      List<WebElement> found = driver.findElements(By.id("R_c" + index));
      if (found.isEmpty()) {
        break;
      }
      candidates.add(found.get(0));
    }
    if (candidates.isEmpty()) {
      throw new NoPlayerMatchException();
    }
    if (candidates.size() == 1) {
      return candidates.get(0);
    }
    // Multiple matches. Check for full text.
    ImmutableList.Builder<String> possible = ImmutableList.builder();
    for (WebElement candidate : candidates) {
      if (candidate.getText().equalsIgnoreCase(fullMatch)) {
        return candidate;
      }
      possible.add(candidate.getText());
    }
    // Nothing matched full text. Make a decent error message, at least.
    throw new AmbiguousPlayerNameException(possible.build());
  }
  
  private void waitForMillis(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
    }
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
