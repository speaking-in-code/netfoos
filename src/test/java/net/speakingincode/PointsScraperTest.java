package net.speakingincode;

import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.google.common.collect.ImmutableList;

import io.github.bonigarcia.wdm.ChromeDriverManager;

public class PointsScraperTest {
  @Test
  public void scrapePoints() throws Exception {
    ChromeDriverManager.getInstance().setup();
    WebDriver driver = new ChromeDriver();
    Credentials credentials = Credentials.load();
    new NetfoosLogin(credentials, driver).login();
    ImmutableList<Player> players = new PointsScraper(driver).getPoints();
    System.out.println(players);
  }
}
