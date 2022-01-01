package net.speakingincode.foos.app;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import net.speakingincode.foos.scrape.ChangeSummarizer;
import net.speakingincode.foos.scrape.CloseableWebDriver;
import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.DamnItLogger;
import net.speakingincode.foos.scrape.EloPointsCalculator;
import net.speakingincode.foos.scrape.NetfoosLogin;
import net.speakingincode.foos.scrape.NetfoosUpdater;
import net.speakingincode.foos.scrape.Player;
import net.speakingincode.foos.scrape.PointsBook;
import net.speakingincode.foos.scrape.PointsUpdater.Mode;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

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
    // PointsBook newPoints = oldPoints.dryRunUpdateAllPlayers(netfoosPlayers);

    // Get the player update summary.
    ChangeSummarizer changes = new ChangeSummarizer(oldPoints.getPlayers(), newPoints.getPlayers());
    String summary = changes.getChangedPlayerSummary();
    logger.info("Writing change summary to : " + getChangeSummaryPath());
    Files.asCharSink(new File(getChangeSummaryPath()), Charsets.UTF_8).write(summary);

    // Write update to console.
    StringBuilder out = new StringBuilder();
    out.append("Local points update:\n");
    out.append(summary);
    out.append("Full points book in " + newPoints.getDestinationUrl() + ".");
    logger.info(out.toString());
  }

  private static ImmutableList<Player> recalculatePlayerPoints(PointsBook pointsBook)
      throws IOException {
    ChromeDriverManager.chromedriver().setup();
    try (CloseableWebDriver driver = new CloseableWebDriver((new ChromeDriver()))) {
      try {
        NetfoosLogin login = new NetfoosLogin(credentials, driver.getDriver());
        login.login();
        return new EloPointsCalculator(pointsBook, driver.getDriver()).getPoints();
      } catch (WebDriverException | IOException e) {
        DamnItLogger.log(driver.getDriver());
        throw e;
      }
    }
  }

  private static String getChangeSummaryPath() {
    return System.getenv("HOME") + "/Desktop/changes.txt";
  }
}
