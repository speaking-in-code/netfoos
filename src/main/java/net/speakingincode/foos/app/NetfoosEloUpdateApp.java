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
import net.speakingincode.foos.scrape.PointsBook;

/**
 * Updates points.
 */
public class NetfoosEloUpdateApp {
  private static final Logger logger = Logger.getLogger(NetfoosEloUpdateApp.class.getName());
  private static Credentials credentials;
  
  public static void main(String[] args) throws IOException {
    PointsBook oldPoints = PointsBook.load();
    credentials = Credentials.load();
    ImmutableList<Player> netfoosPlayers = recalculatePlayerPoints(oldPoints);

    logger.info("Updating netfoos.");
    new NetfoosUpdater(credentials, Mode.LOCAL).runUpdates(netfoosPlayers);
    
    logger.info("Publishing to Google Sheets.");
    PointsBook newPoints = oldPoints.updateAllPlayers(netfoosPlayers);

    // Get the player update summary.
    ChangeSummarizer changes = new ChangeSummarizer(oldPoints.getPlayers(), newPoints.getPlayers());
    String summary = changes.getChangedPlayerSummary();
    logger.info("Writing change summary to : " + getChangeSummaryPath());
    Files.write(summary, new File(getChangeSummaryPath()), Charsets.UTF_8);
    
    // Write update to console.
    StringBuilder out = new StringBuilder();
    out.append("Local points update:\n");
    out.append(summary);
    out.append("Full points book in " + newPoints.getDestinationUrl() + ".");
    logger.info(out.toString());
  }
  
  private static ImmutableList<Player> recalculatePlayerPoints(PointsBook pointsBook)
      throws IOException {
    ChromeDriverManager.getInstance().setup();
    WebDriver driver = null;
    try {
      driver = new ChromeDriver();
      NetfoosLogin login = new NetfoosLogin(credentials, driver);
      login.login();
      return new EloPointsCalculator(pointsBook, driver).getPoints();
    } finally {
      driver.close();
    }
  }
  
  private static String getChangeSummaryPath() {
    return System.getenv("HOME") + "/Desktop/changes.txt";
  }
}
