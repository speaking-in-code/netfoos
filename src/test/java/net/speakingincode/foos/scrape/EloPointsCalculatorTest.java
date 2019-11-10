package net.speakingincode.foos.scrape;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;

public class EloPointsCalculatorTest {
  private static Credentials credentials = Credentials.load();
  private WebDriver driver;
  private EloPointsCalculator calculator;

  @Before
  public void before() throws Exception {
    ChromeDriverManager.chromedriver().setup();
    driver = new ChromeDriver();
    calculator = new EloPointsCalculator(PointsBookFixture.emptyPointsBook(), driver);
    NetfoosLogin login = new NetfoosLogin(credentials, driver);
    login.login();
  }

  @After
  public void after() {
    driver.close();
  }

  @Test
  public void scrapeBrian() throws Exception {
    String player = "Eaton, Brian";
    String output = calculator.getPointsReport(player);
    String path = System.getenv("HOME") + "/Desktop/" + player + ".html";
    Files.asCharSink(new File(path), Charsets.UTF_8).write(output);
  }

  @Test(expected = IOException.class)
  public void scrapeNonExistent() throws Exception {
    calculator.getPointsReport("Player, Not Found");
  }
}
