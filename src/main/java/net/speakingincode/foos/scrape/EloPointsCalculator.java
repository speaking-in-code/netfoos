package net.speakingincode.foos.scrape;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.google.common.collect.ImmutableList;

public class EloPointsCalculator {
  private static final Logger logger = Logger.getLogger(EloPointsCalculator.class.getName());
  private final WebDriver driver;
  private final PointsParser parser = new PointsParser();
  
  public EloPointsCalculator(WebDriver driver) {
    this.driver = driver;
  }
  
  public ImmutableList<Player> getPoints() throws IOException {
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
    setTuningParameters();
    logger.info("Starting Elo Points Calculation.");
    driver.findElement(By.name("Submit")).click();
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
  
  private void setTuningParameters() {
    // Default points for a new player.
    WebElement defaultPoints = driver.findElement(By.id("nfts_mod_4"));
    defaultPoints.clear();
    defaultPoints.sendKeys("700");
    // Minimum points a player can have.
    WebElement cutoffPoints = driver.findElement(By.id("nfts_mod_8"));
    cutoffPoints.clear();
    cutoffPoints.sendKeys("600");
    Select bonusPoints = new Select(driver.findElement(By.name("nfts_mod_9")));
    // Bonus points for winning. Setting this wrong can make local points diverge from IFP
    // points very quickly.
    // This is "K / (Finish * 16) (Awards least points)"
    bonusPoints.selectByValue("16");
  }
}