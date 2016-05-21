package net.speakingincode.foos.app;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import net.speakingincode.foos.scrape.ChangeSummarizer;
import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.EloPointsCalculator;
import net.speakingincode.foos.scrape.NetfoosLogin;
import net.speakingincode.foos.scrape.NetfoosUpdater;
import net.speakingincode.foos.scrape.Player;
import net.speakingincode.foos.scrape.PointsUpdater.Mode;
import net.speakingincode.foos.scrape.SpreadsheetOutput;

/**
 * Updates points.
 */
public class NetfoosEloUpdateApp {
  private static final Logger logger = Logger.getLogger(NetfoosEloUpdateApp.class.getName());
  private static Credentials credentials;
  
  public static void main(String[] args) throws IOException {
    WebDriver driver = null;
    try {
      ChromeDriverManager.getInstance().setup();
      credentials = Credentials.load();
      driver = new ChromeDriver();
      NetfoosLogin login = new NetfoosLogin(credentials, driver);
      login.login();
      ImmutableList<Player> players = new EloPointsCalculator(driver).getPoints();
      String summary = new ChangeSummarizer(players).getChangedPlayerSummary();
      logger.info("Writing change summary to : " + getChangeSummaryPath());
      Files.write(summary, new File(getChangeSummaryPath()), Charsets.UTF_8);
      logger.info("Publishing to Google Sheets.");
      SpreadsheetOutput spreadsheet = new SpreadsheetOutput(players);
      spreadsheet.publishToGoogleSheets();
      new NetfoosUpdater(credentials, Mode.LOCAL).runUpdates(players);
      StringBuilder out = new StringBuilder();
      out.append("Local points update:\n");
      out.append(Files.toString(new File(getChangeSummaryPath()), Charsets.UTF_8));
      out.append("Full points book in " + spreadsheet.getDestinationUrl() + ".");
      logger.info(out.toString());
    } finally {
      driver.close();
    }
  }
  
  private static String getChangeSummaryPath() {
    return System.getenv("HOME") + "/Desktop/changes.txt";
  }
}
