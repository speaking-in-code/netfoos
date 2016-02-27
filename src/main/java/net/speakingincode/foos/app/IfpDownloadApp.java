package net.speakingincode.foos.app;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.IfpScraper;
import net.speakingincode.foos.scrape.NetfoosLogin;
import net.speakingincode.foos.scrape.Player;
import net.speakingincode.foos.scrape.PointsScraper;
import net.speakingincode.foos.scrape.WorkerPool;

public class IfpDownloadApp {
  private static final Logger logger = Logger.getLogger(IfpDownloadApp.class.getName());
  private static final File PLAYER_LIST_CACHE =
      new File(System.getenv("HOME") + "/.netfoosplayers");
  private static final int PARALLEL_THREADS = 10;
  
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
    storePlayerTable(players);
  }
  
  private static Map<String, Integer> getPlayerTable() throws IOException {
    if (PLAYER_LIST_CACHE.exists()) {
      logger.info("Reusing player list cache from " + PLAYER_LIST_CACHE);
      return readPlayerTable();
    }
    WebDriver driver = null;
    try {
      driver = new ChromeDriver();
      NetfoosLogin login = new NetfoosLogin(Credentials.load(), driver);
      login.login();
      ImmutableList<Player> players = new PointsScraper(driver).getPoints();
      Map<String, Integer> points = Maps.newHashMap();
      for (Player player : players) {
        points.put(player.name(), -1);
      }
      storePlayerTable(points);
      return points;
    } finally {
      driver.close();
    }
  }
  
  private static Map<String, Integer> readPlayerTable() throws IOException {
    try (Reader reader = Files.newBufferedReader(PLAYER_LIST_CACHE.toPath())) {
      Properties props = new Properties();
      props.load(reader);
      Map<String, Integer> players = Maps.newHashMap();
      for (Entry<Object, Object> entry : props.entrySet()) {
        players.put(entry.getKey().toString(), Integer.parseInt(entry.getValue().toString()));
      }
      return players;
    }  
  }
  
  private static void storePlayerTable(Map<String, Integer> map) throws IOException {
    Properties props = new Properties();
    for (Entry<String, Integer> entry : map.entrySet()) {
      props.setProperty(entry.getKey(), "" + entry.getValue());
    }
    logger.info("Writing player list cache to " + PLAYER_LIST_CACHE);
    try (Writer writer = Files.newBufferedWriter(PLAYER_LIST_CACHE.toPath())) {
      props.store(writer, "");
    }
  }
}
