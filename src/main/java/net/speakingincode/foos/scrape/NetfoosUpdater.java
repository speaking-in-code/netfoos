package net.speakingincode.foos.scrape;

import java.io.IOException;
import java.util.logging.Logger;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.common.collect.ImmutableList;

/**
 * Sends updates to netfoos.
 */
public class NetfoosUpdater {
  private static final Logger logger = Logger.getLogger(NetfoosUpdater.class.getName());
  private static final int PARALLEL_UPDATES = 4;

  private final Credentials credentials;
  private final PointsUpdater.Mode mode;
  
  public NetfoosUpdater(Credentials credentials, PointsUpdater.Mode mode) {
    this.credentials = credentials;
    this.mode = mode;
  }
  
  /**
   * Updates netfoos with any changes.
   * 
   * @param players list of all players. Only changes will be sent to netfoos.
   */
  public void runUpdates(ImmutableList<Player> players) {
    ImmutableList<Player> changed = new PointsDiffer().findChangedPlayers(players, mode);
    logger.info("Starting update of " + changed.size() + " players.");
    WorkerPool<Player, Boolean> work = WorkerPool.create(PARALLEL_UPDATES, new UpdaterFactory());
    work.parallelDo(changed);
    logger.info("Updates complete.");
  }
  
  private class UpdaterFactory implements Worker.Factory<Player, Boolean> {
    @Override
    public Worker<Player, Boolean> newWorker() {
      return new Updater();
    }
  }
  
  private class Updater implements Worker<Player, Boolean> {
    private final HtmlUnitDriver driver;
    private final PointsUpdater updater;
    
    public Updater() {
      driver = new HtmlUnitDriver();
      NetfoosLogin login = new NetfoosLogin(credentials, driver);
      try {
        login.login();
        updater = new PointsUpdater(driver, mode);
        updater.beginUpdate();
      } catch (IOException e) {
        throw new RuntimeException("Failed netfoos login", e);
      }
    }

    @Override
    public void shutdown() {
      driver.close();
    }

    @Override
    public Boolean convert(Player x) {
      try {
        logger.info("Updating: " + x);
        updater.updatePoints(x);
      } catch (IOException e) {
        return false;
      }
      return true;
    }
  }
}
