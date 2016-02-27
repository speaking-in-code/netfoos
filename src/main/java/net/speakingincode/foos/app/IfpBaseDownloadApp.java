package net.speakingincode.foos.app;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.IfpCache;
import net.speakingincode.foos.scrape.IfpScraper;
import net.speakingincode.foos.scrape.NetfoosLogin;
import net.speakingincode.foos.scrape.Player;
import net.speakingincode.foos.scrape.EloPointsCalculator;
import net.speakingincode.foos.scrape.WorkerPool;

public class IfpBaseDownloadApp {
  private static final Logger logger = Logger.getLogger(IfpBaseDownloadApp.class.getName());
  private static final int PARALLEL_THREADS = 10;
  private static final IfpCache ifpCache = new IfpCache();
  
  public static void main(String[] args) throws IOException {
    ChromeDriverManager.getInstance().setup();
    Map<String, Integer> players = getPlayerTable();
    List<String> toScrape = Lists.newArrayList();
    for (Entry<String, Integer> player : players.entrySet()) {
      if (player.getValue() == -1) {
        toScrape.add(player.getKey());
      }
    }
    logger.info("Scraping IFP points for " + toScrape.size() + " players.");
    WorkerPool<String, Integer> pool = WorkerPool.create(PARALLEL_THREADS, IfpScraper.factory());
    Map<String, Integer> newPoints = pool.parallelDo(toScrape);
    int found = 0;
    for (Integer points : newPoints.values()) {
      if (points != -1) {
        ++found;
      }
    }
    logger.info(String.format("Successfully scraped %d/%d players.", found, toScrape.size()));
    players.putAll(newPoints);
    ifpCache.storePlayerTable(players);
  }
  
  private static Map<String, Integer> getPlayerTable() throws IOException {
    try {
      Map<String, Integer> table = ifpCache.readPlayerTable();
      logger.info("Reusing player list cache");
      return table;
    } catch (IOException e) {
      logger.info("No player cache available, refreshing from netfoos.");
    }
    WebDriver driver = null;
    try {
      driver = new ChromeDriver();
      NetfoosLogin login = new NetfoosLogin(Credentials.load(), driver);
      login.login();
      ImmutableList<Player> players = new EloPointsCalculator(driver).getPoints();
      Map<String, Integer> points = Maps.newHashMap();
      for (Player player : players) {
        points.put(player.name(), -1);
      }
      ifpCache.storePlayerTable(points);
      return points;
    } finally {
      driver.close();
    }
  }
}
