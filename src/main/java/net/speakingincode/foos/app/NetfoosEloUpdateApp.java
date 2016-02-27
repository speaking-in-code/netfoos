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
import net.speakingincode.foos.scrape.NetfoosLogin;
import net.speakingincode.foos.scrape.NetfoosUpdater;
import net.speakingincode.foos.scrape.Player;
import net.speakingincode.foos.scrape.PointsScraper;
import net.speakingincode.foos.scrape.SpreadsheetOutput;

/**
 * Updates points.
 */
public class NetfoosEloUpdateApp {
  private static final boolean DRY_RUN = false;
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
      ImmutableList<Player> players = new PointsScraper(driver).getPoints();
      String summary = new ChangeSummarizer(players).getChangedPlayerSummary();
      logger.info("Change summary:\n" + summary);
      String path = getSpreadsheetOutputPath();
      logger.info("Writing all points " + path);
      String sheet = new SpreadsheetOutput(players).getOutput();
      Files.write(sheet, new File(path), Charsets.UTF_8);
      if (!DRY_RUN) {
        new NetfoosUpdater(credentials).runUpdates(players);
      }
    } finally {
      driver.close();
    }
  }

  private static String getSpreadsheetOutputPath() {
    return System.getenv("HOME") + "/Desktop/points.tsv";
  }
}
