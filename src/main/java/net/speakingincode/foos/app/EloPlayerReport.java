package net.speakingincode.foos.app;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import net.speakingincode.foos.scrape.PointsBook;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.EloPointsCalculator;
import net.speakingincode.foos.scrape.NetfoosLogin;

/**
 * Updates points.
 */
public class EloPlayerReport {
  private static final Logger logger = Logger.getLogger(EloPlayerReport.class.getName());
  private static Credentials credentials;
  private static WebDriver driver;
  
  public static void main(String[] args) throws IOException {
    credentials = Credentials.load();
    ChromeDriverManager.getInstance().setup();
    PointsBook pointsBook = PointsBook.load();
    try {
      driver = new ChromeDriver();
      NetfoosLogin login = new NetfoosLogin(credentials, driver);
      login.login();
      for (String netfoosName : args) {
        logger.info("Generating report for " + netfoosName);
        try {
          writeReport(pointsBook, netfoosName);
        } catch (IOException e) {
          logger.warning("Failed to generate reort for " + netfoosName);
        }
      }
    } finally {
      driver.close();
    }
  }
  
  private static void writeReport(PointsBook pointsBook, String netfoosName) throws IOException {
    String html = new EloPointsCalculator(pointsBook, driver).getPointsReport(netfoosName);
    String path = getChangeSummaryDirectory() + "/" + netfoosName + ".html";
    Files.write(html, new File(path), Charsets.UTF_8);
  }
  
  private static String getChangeSummaryDirectory() {
    return System.getenv("HOME") + "/Desktop";
  }
}
