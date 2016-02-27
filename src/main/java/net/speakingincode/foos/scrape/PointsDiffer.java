package net.speakingincode.foos.scrape;

import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;

import net.speakingincode.foos.scrape.PointsUpdater.Mode;

public class PointsDiffer {
  private static final Logger log = Logger.getLogger(PointsDiffer.class.getName());
  public PointsDiffer() {}
  
  public ImmutableList<Player> findChangedPlayers(ImmutableList<Player> players, Mode mode) {
    ImmutableList.Builder<Player> changed = ImmutableList.builder();
    for (Player p : players) {
      if (mode == Mode.LOCAL) {
        if (p.oldPoints() != p.newPoints()) {
          changed.add(p);
        }
      } else {
        if (p.oldBasePoints() != p.newBasePoints()) {
          log.info("Base points change for " + p);
          changed.add(p);
        }
      }
    }
    return changed.build();
  }
}
