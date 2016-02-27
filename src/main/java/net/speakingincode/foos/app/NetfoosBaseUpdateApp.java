package net.speakingincode.foos.app;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.google.common.collect.ImmutableList;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.EloPointsCalculator;
import net.speakingincode.foos.scrape.IfpCache;
import net.speakingincode.foos.scrape.NetfoosLogin;
import net.speakingincode.foos.scrape.NetfoosUpdater;
import net.speakingincode.foos.scrape.Player;
import net.speakingincode.foos.scrape.PointsUpdater.Mode;

public class NetfoosBaseUpdateApp {
  private static final Logger logger = Logger.getLogger(NetfoosBaseUpdateApp.class.getName());
  
  public static void main(String[] args) throws IOException {
    Map<String, Integer> ifpPoints;
    try {
      ifpPoints = new IfpCache().readPlayerTable();
    } catch (IOException e) {
      logger.severe("No IFP points cache found. Try running IfpDownloadApp.");
      return;
    }
    ChromeDriverManager.getInstance().setup();
    WebDriver driver = new ChromeDriver();
    try {
      Credentials credentials = Credentials.load();
      NetfoosLogin login = new NetfoosLogin(credentials, driver);
      login.login();
      ImmutableList<Player> oldBase = new EloPointsCalculator(driver).getPoints();
      ImmutableList.Builder<Player> newBase = ImmutableList.builder();
      for (Player old : oldBase) {
        Integer ifp = ifpPoints.get(old.name());
        if (ifp != null && ifp != 0) {
          newBase.add(old.toBuilder().newBasePoints(ifp).build());
        }
      }
      new NetfoosUpdater(credentials, Mode.BASE).runUpdates(newBase.build());
    } finally {
      driver.close();
    }
  }
}
