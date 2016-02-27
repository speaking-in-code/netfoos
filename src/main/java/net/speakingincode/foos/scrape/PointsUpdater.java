package net.speakingincode.foos.scrape;

import java.io.IOException;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PointsUpdater {
  private static final Logger log = Logger.getLogger(PointsUpdater.class.getName());
  private static final boolean DRY_RUN = false;
  private final WebDriver driver;
  private final Mode mode;
  
  public static enum Mode {
    /** Update points used for local seeding. */
    LOCAL,
    /** Update points used as base for Elo calculation. */
    BASE
  }
  
  public PointsUpdater(WebDriver driver, Mode mode) {
    this.driver = driver;
    this.mode = mode;
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
    String name = player.name();
    checkOnManagePlayers();
    WebElement edit = driver.findElement(By.partialLinkText(name));
    edit.click();
    WebElement pointsField;
    int numPoints;
    if (mode == Mode.LOCAL) {
      pointsField = driver.findElement(By.id("pointsother"));
      numPoints = player.newPoints();
    } else {
      pointsField = driver.findElement(By.id("dob"));
      numPoints = player.newBasePoints();
    }
    if (DRY_RUN) {
      return;
    }
    String points = intToString(numPoints);
    pointsField.clear();
    pointsField.sendKeys(points);
    WebElement submit = driver.findElement(By.name("Submit"));
    submit.click();
    String page = driver.getPageSource();
    if (!page.contains("Player Updated Successfully")) {
      throw new IOException("Couldn't update player " + name + ": " + page);
    }
    log.info("Updated player " + mode.toString().toLowerCase() + ": " + name + ": " + points);
  }
  
  private String intToString(int numPoints) {
    if (numPoints == 0) {
      return "";
    }
    return Integer.toString(numPoints);
  }

  private void checkOnManagePlayers() throws IOException {
    String page = driver.getPageSource();
    if (!page.contains("Name (click to edit)")) {
      throw new IOException("Couldn't find manage players page: " + page);
    }
  }
}
