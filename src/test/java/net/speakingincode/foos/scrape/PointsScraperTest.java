package net.speakingincode.foos.scrape;

import com.google.common.collect.ImmutableList;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class PointsScraperTest {
  @Test
  public void scrapePoints() throws Exception {
    ChromeDriverManager.chromedriver().setup();
    WebDriver driver = new ChromeDriver();
    try {
      Credentials credentials = Credentials.load();
      new NetfoosLogin(credentials, driver).login();
      PointsBook pointsBook = PointsBookFixture.emptyPointsBook();
      ImmutableList<Player> players = new EloPointsCalculator(pointsBook, driver).getPoints();
      MatcherAssert.assertThat(players.size(), Matchers.greaterThan(100));
    } finally {
      driver.close();
    }
  }
}
