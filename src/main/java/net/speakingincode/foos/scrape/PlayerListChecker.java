package net.speakingincode.foos.scrape;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
      List<WebElement> links = driver.findElements(MoreBy.linkPlayerName(player));
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

  public NetfoosTable loadNetfoosPlayers() throws IOException {
    ImmutableMap.Builder<String, Player> out = ImmutableMap.builder();
    new NetfoosLogin(credentials, driver).login();
    WebElement manage = driver.findElement(By.linkText("Manage Players"));
    if (manage == null) {
      throw new IOException("Not on main page: " + driver.getPageSource());
    }
    manage.click();
    checkOnManagePlayers();
    List<WebElement> tableRows = driver.findElements(By.tagName("tr"));
    for (WebElement row : tableRows) {
      List<WebElement> fields = row.findElements(By.tagName("td"));
      if (fields.size() != 6 || !"Delete".equals(fields.get(5).getText())) {
        continue;
      }
      String name = fields.get(0).getText();
      if (name.contains("click to edit")) {
        continue;
      }
      int pointsBase = parsePoints(fields.get(3).getText());
      int localPoints = parsePoints(fields.get(4).getText());
      out.put(name.toLowerCase(Locale.ROOT), Player.builder()
          .name(name)
          .oldPoints(localPoints)
          .newPoints(localPoints)
          .oldBasePoints(pointsBase)
          .newBasePoints(pointsBase)
          .build());
    }
    return new NetfoosTable(out.build());
  }

  private int parsePoints(String pointsStr) {
    if ("-".equals(pointsStr)) {
      return 0;
    }
    return Integer.parseInt(pointsStr);
  }
}
