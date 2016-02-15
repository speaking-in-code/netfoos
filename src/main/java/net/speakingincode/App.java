package net.speakingincode;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import io.github.bonigarcia.wdm.ChromeDriverManager;

/**
 * Updates points.
 */
public class App {
  private static final Logger logger = Logger.getLogger(App.class.getName());
  private static final int PARALLEL_UPDATES = 4;
  
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
      ImmutableList<Player> changed = new PointsDiffer().findChangedPlayers(players);
      logger.info("Starting update of " + changed.size() + " players.");
      runUpdates(changed);
      logger.info("Updates complete.");
    } finally {
      driver.close();
    }
  }
  
  private static void runUpdates(ImmutableList<Player> changed) {
    Queue<Player> workQueue = Queues.newConcurrentLinkedQueue();
    workQueue.addAll(changed);
    ListeningExecutorService executor = MoreExecutors.listeningDecorator(
        Executors.newFixedThreadPool(PARALLEL_UPDATES));
    List<ListenableFuture<Void>> updaters = Lists.newArrayList();
    for (int i = 0; i < PARALLEL_UPDATES; ++i) {
      ListenableFuture<Void> update = executor.submit(new Updater(workQueue));
      updaters.add(update);
    }

    try {
      for (ListenableFuture<Void> update : updaters) {
        update.get();
      }
    } catch (InterruptedException e) {
      logger.severe("Interrupted running updates: " + e);
    } catch (ExecutionException e) {
      logger.severe("Error running updates: " + e);
    }
  }
  
  private static class Updater implements Callable<Void> {
    private final Queue<Player> workQueue;

    public Updater(Queue<Player> workQueue) {
      this.workQueue = workQueue;
    }

    @Override
    public Void call() throws Exception {
      WebDriver driver = new HtmlUnitDriver();
      try {
        NetfoosLogin login = new NetfoosLogin(credentials, driver);
        login.login();
        PointsUpdater updater = new PointsUpdater(driver);
        updater.beginUpdate();
        for (Player p = workQueue.poll(); p != null; p = workQueue.poll()) {
          updater.updatePoints(p);
        }
      } finally {
        driver.close();
      }
      return null;
    }
  }
}
