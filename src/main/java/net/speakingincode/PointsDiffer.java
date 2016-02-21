package net.speakingincode;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

public class PointsDiffer {
  public PointsDiffer() {}
  
  public ImmutableList<Player> findChangedPlayers(ImmutableList<Player> players) {
    ImmutableList.Builder<Player> changed = ImmutableList.builder();
    for (Player p : players) {
      // Objects.equal handles null properly.
      if (!Objects.equal(p.oldPoints(), p.newPoints())) {
        changed.add(p);
      }
    }
    return changed.build();
  }
}
