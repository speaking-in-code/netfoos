package net.speakingincode.foos.scrape;

import java.io.IOException;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PointsUpdater {
  private static final Logger log = Logger.getLogger(PointsUpdater.class.getName());
  private final WebDriver driver;

  public PointsUpdater(WebDriver driver) {
    this.driver = driver;
  }
  
  public void beginUpdate() throws IOException {
    WebElement manage = driver.findElement(By.linkText("Manage Players"));
    if (manage == null) {
      throw new IOException("Not on main page: " + driver.getPageSource());
    }
    manage.click();
    checkOnManagePlayers();
  }
  
  public void updatePoints(Player player) throws IOException {
    checkOnManagePlayers();
    WebElement edit = driver.findElement(By.partialLinkText(player.name()));
    edit.click();
    WebElement localPoints = driver.findElement(By.id("pointsother"));
    String points;
    if (player.newPoints() == 0) {
      points = "";
    } else {
      points = Integer.toString(player.newPoints());
    }
    localPoints.clear();
    localPoints.sendKeys(points);
    WebElement submit = driver.findElement(By.name("Submit"));
    submit.click();
    String page = driver.getPageSource();
    if (!page.contains("Player Updated Successfully")) {
      throw new IOException("Couldn't update player " + player.name() + ": " + page);
    }
    log.info("Updated player: " + player.name() + ": " + points);
  }
  
  private void checkOnManagePlayers() throws IOException {
    String page = driver.getPageSource();
    if (!page.contains("Name (click to edit)")) {
      throw new IOException("Couldn't find manage players page: " + page);
    }
  }
}
