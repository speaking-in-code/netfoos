package net.speakingincode.foos.scrape;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.collect.ImmutableSet;

public class PlayerListChecker {
  private final Credentials credentials;
  private final WebDriver driver;
  
  public PlayerListChecker(Credentials credentials, WebDriver driver) {
    this.credentials = credentials;
    this.driver = driver;
  }
  
  /**
   * Checks to verify that all players in the provided list exist in NetFoos.
   * 
   * @param players
   * @return set of missing players
   * @throws IOException
   */
  public ImmutableSet<String> findMissingPlayers(Iterable<String> players)
      throws IOException {
    new NetfoosLogin(credentials, driver).login();
    ImmutableSet.Builder<String> notFound = ImmutableSet.builder();
    WebElement manage = driver.findElement(By.linkText("Manage Players"));
    if (manage == null) {
      throw new IOException("Not on main page: " + driver.getPageSource());
    }
    manage.click();
    checkOnManagePlayers();
    for (String player : players) {
      // No nick names allowed.
      if (player.contains("\"")) {
        notFound.add(player);
        continue;
      }
      List<WebElement> links = driver.findElements(MoreBy.linkTextPrefix(player));
      if (links.size() != 1) {
        notFound.add(player);
      }
    }
    return notFound.build();
  }
  
  private void checkOnManagePlayers() throws IOException {
    String page = driver.getPageSource();
    if (!page.contains("Name (click to edit)")) {
      throw new IOException("Couldn't find manage players page: " + page);
    }
  }
}
