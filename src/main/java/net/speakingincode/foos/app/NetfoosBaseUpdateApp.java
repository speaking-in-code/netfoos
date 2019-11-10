package net.speakingincode.foos.app;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.EloPointsCalculator;
import net.speakingincode.foos.scrape.IfpScraper;
import net.speakingincode.foos.scrape.NetfoosLogin;
import net.speakingincode.foos.scrape.NetfoosUpdater;
import net.speakingincode.foos.scrape.Player;
import net.speakingincode.foos.scrape.PointsBook;
import net.speakingincode.foos.scrape.PointsBookPlayer;
import net.speakingincode.foos.scrape.PointsUpdater.Mode;
import net.speakingincode.foos.scrape.WorkerPool;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

public class NetfoosBaseUpdateApp {
  private static final Logger logger = Logger.getLogger(NetfoosBaseUpdateApp.class.getName());
  private static final int PARALLEL_THREADS = 10;

  public static void main(String[] args) throws IOException {
    ChromeDriverManager.chromedriver().setup();
    PointsBook oldPoints = PointsBook.load();
    Map<String, Integer> ifpTable = getCurrentIfpPoints(oldPoints);
    updateNetfoosBasePoints(oldPoints, ifpTable);
  }

  /**
   * PointsBook: has both local name and IFP name.
   * oldBase/newBase: only has local name
   * newPoints: only has IFP name.
   */
  private static void updateNetfoosBasePoints(PointsBook pointsBook, Map<String, Integer> ifpTable)
      throws IOException {
    WebDriver driver = new ChromeDriver();
    try {
      Credentials credentials = Credentials.load();
      NetfoosLogin login = new NetfoosLogin(credentials, driver);
      login.login();

      ImmutableList<Player> oldBase = new EloPointsCalculator(pointsBook, driver).getPoints();
      Map<String, Player> localNameToPlayer = Maps.newHashMap();
      for (Player player : oldBase) {
        localNameToPlayer.put(player.name(), player);
      }

      ImmutableList.Builder<Player> updates = ImmutableList.builder();
      for (PointsBookPlayer bookPlayer : pointsBook.getPlayers()) {
        Player currentLocal = localNameToPlayer.get(bookPlayer.name());
        Integer currentIfp = ifpTable.get(bookPlayer.ifpId());
        System.out.println("Book player: " + bookPlayer + ": " + currentIfp);
        System.out.println("Current local: " + currentLocal);
        if (currentIfp != null && currentLocal.oldBasePoints() != currentIfp) {
          updates.add(currentLocal.toBuilder().newBasePoints(currentIfp).build());
        }
      }
      new NetfoosUpdater(credentials, Mode.BASE).runUpdates(updates.build());
    } finally {
      driver.close();
    }
  }

  private static Map<String, Integer> getCurrentIfpPoints(PointsBook oldPoints) throws IOException {
    Set<String> toScrape = Sets.newHashSet();
    for (PointsBookPlayer player : oldPoints.getPlayers()) {
      if (!player.ifpId().trim().equals(player.ifpId())) {
        throw new IOException(String.format(
            "Leading or trailing whitespace on IFP id [%s]", player.ifpId()));
      }
      if (!player.ifpId().isEmpty()) {
        toScrape.add(player.ifpId());
      }
    }
    logger.info("Scraping IFP points for " + toScrape.size() + " players.");
    WorkerPool<String, Integer> pool = WorkerPool.create(PARALLEL_THREADS, IfpScraper.factory());
    Map<String, Integer> newPoints = pool.parallelDo(toScrape);
    StringBuilder errors = new StringBuilder();
    for (Entry<String, Integer> ifpPlayer : newPoints.entrySet()) {
      if (ifpPlayer.getValue() == 0) {
        errors.append("No points found for '" + ifpPlayer.getKey() + "'.\n");
      }
    }
    if (errors.length() != 0) {
      throw new IOException("Error reading IFP points:\n" + errors);
    }
    return newPoints;
  }
}
