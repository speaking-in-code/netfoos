package net.speakingincode.foos.scrape;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.google.common.collect.ImmutableList;

public class EloPointsCalculator {
  private static final Logger logger = Logger.getLogger(EloPointsCalculator.class.getName());
  private static final ImmutableList<String> LINKS = ImmutableList.of(
      "/images/blue_box.gif", "/images_admin/10spacer.gif");

  private final PointsParser parser;
  private final WebDriver driver;

  public EloPointsCalculator(PointsBook pointsBook, WebDriver driver) {
    this.parser = new PointsParser(pointsBook);
    this.driver = driver;
  }
  
  /**
   * Recalculates points for all players.
   * 
   * @return points for all players in the netfoos database.
   */
  public ImmutableList<Player> getPoints() throws IOException {
    navigateToEloPage();
    setTuningParameters();
    startCalculation();
    WebElement points = driver.findElement(By.tagName("textarea"));
    ImmutableList.Builder<Player> players = ImmutableList.builder();
    try (BufferedReader reader = new BufferedReader(new StringReader(points.getText()))) {
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        Player p = parser.parse(line);
        if (p != null) {
          players.add(p);
        }
      }
    }
    return players.build();
  }
  
  /**
   * Gets points report for a single player.
   * 
   * @return the HTML for the points report as a single page.
   * 
   * @throws IOException if the report can't be generated.
   */
  public String getPointsReport(String netfoosName) throws IOException {
    navigateToEloPage();
    setTuningParameters();
    selectPlayer(netfoosName);
    startCalculation();
    String source = driver.getPageSource();
    if (!source.contains("Summary: " + netfoosName)) {
      throw new IOException("Could not generate summary. Output was:\n" + source);
    }
    // Replace the relative links to the graph images with absolute links.
    for (String link : LINKS) {
      Pattern p = Pattern.compile(link, Pattern.LITERAL);
      String replace = Matcher.quoteReplacement("http://www.netfoos.com" + link);
      source = p.matcher(source).replaceAll(replace);
    }
    return source;
  }
  
  private void navigateToEloPage() throws IOException {
    driver.findElement(By.linkText("Admin Modules")).click();
    String page = driver.getPageSource();
    if (!page.contains("NetFoos Admin Modules")) {
      throw new IOException("Not on admin modules page: " + page);
    }
    driver.findElement(By.partialLinkText("ELO_Points_Calculations")).click();
    page = driver.getPageSource();
    if (!page.contains("Show All Players Current ELO Points")) {
      throw new IOException("Not on ELO module page: " + page);
    }
  }
  
  private void selectPlayer(String netfoosName) throws IOException {
    Select reportType = new Select(driver.findElement(By.name("nfts_mod_5")));
    try {
      reportType.selectByVisibleText(netfoosName);
    } catch (NoSuchElementException e) {
      throw new IOException(e);
    }
  }
  
  private void setTuningParameters() {
    // Default points for a new player.
    WebElement defaultPoints = driver.findElement(By.id("nfts_mod_4"));
    defaultPoints.clear();
    defaultPoints.sendKeys("700");
    // K value. Leave blank so that we can use K values from individual events
    // instead.
    WebElement kValue = driver.findElement(By.id("nfts_mod_6"));
    kValue.clear();
    // Minimum points a player can have.
    WebElement cutoffPoints = driver.findElement(By.id("nfts_mod_8"));
    cutoffPoints.clear();
    cutoffPoints.sendKeys("600");
    Select bonusPoints = new Select(driver.findElement(By.name("nfts_mod_9")));
    // Bonus points for winning. Setting this wrong can make local points diverge from IFP
    // points very quickly.
    // This is "K / (Finish * 8) (Awards least points)"
    bonusPoints.selectByValue("8");
  }
  
  private void startCalculation() {
    logger.info("Starting Elo Points Calculation.");
    driver.findElement(By.name("Submit")).click();
  }
}
