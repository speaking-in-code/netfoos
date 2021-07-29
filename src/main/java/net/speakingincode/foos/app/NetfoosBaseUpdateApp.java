package net.speakingincode.foos.app;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.EloPointsCalculator;
import net.speakingincode.foos.scrape.IfpPoints;
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
import java.util.List;
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
    IfpPoints ifpTable = IfpPoints.load();
    validateIfpIds(oldPoints, ifpTable);
    updateNetfoosBasePoints(oldPoints, ifpTable);
  }

  private static void validateIfpIds(PointsBook pointsBook, IfpPoints ifpTable) throws IOException {
    List<String> mismatches = Lists.newArrayList();
    for (PointsBookPlayer bookPlayer : pointsBook.getPlayers()) {
      if (bookPlayer.ifpId() == null || bookPlayer.ifpId().isEmpty()) {
        continue;
      }
      Integer ifpPoints = ifpTable.getPoints(bookPlayer.ifpId());
      if (ifpPoints == null) {
        mismatches.add(bookPlayer.ifpId());
      }
    }
    if (mismatches.isEmpty()) {
      return;
    }
    throw new IOException("Mismatched IFP names: " + Joiner.on("\n").join(mismatches));
  }

  /**
   * PointsBook: has both local name and IFP name.
   * oldBase/newBase: only has local name
   * newPoints: only has IFP name.
   */
  private static void updateNetfoosBasePoints(PointsBook pointsBook, IfpPoints ifpTable)
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
        Integer currentIfp = ifpTable.getPoints(bookPlayer.ifpId());
        System.out.println("Book player: " + bookPlayer + ": " + currentIfp);
        System.out.println("Current local: " + currentLocal);
        if (currentIfp != null && currentIfp > 0 && currentLocal.oldBasePoints() != currentIfp) {
          updates.add(currentLocal.toBuilder().newBasePoints(currentIfp).build());
        }
      }
      new NetfoosUpdater(credentials, Mode.BASE).runUpdates(updates.build());
    } finally {
      driver.close();
    }
  }
}
