package net.speakingincode.foos.scrape;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import io.github.bonigarcia.wdm.ChromeDriverManager;

public class EloPointsCalculatorTest {
  private static Credentials credentials = Credentials.load();
  private WebDriver driver;
  private EloPointsCalculator calculator;
  
  @Before
  public void before() throws Exception {
    ChromeDriverManager.getInstance().setup();
    driver = new ChromeDriver();
    calculator = new EloPointsCalculator(driver);
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
    Files.write(output, new File(path), Charsets.UTF_8);
  }
  
  @Test(expected = IOException.class)
  public void scrapeNonExistent() throws Exception {
    calculator.getPointsReport("Player, Not Found");
  }
}
