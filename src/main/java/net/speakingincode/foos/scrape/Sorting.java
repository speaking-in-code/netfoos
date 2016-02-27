package net.speakingincode.foos.scrape;

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

public class Sorting {
  private Sorting() {}
  
  public static ImmutableList<Player> copySortedByOldPoints(Collection<Player> players) {
    return byOldPoints.immutableSortedCopy(players);
  }
  
  public static ImmutableList<Player> copySortedByNewPoints(Collection<Player> players) {
    return byNewPoints.immutableSortedCopy(players);
  }
  
  private static final Ordering<Player> byOldPoints = new Ordering<Player>() {
    @Override
    public int compare(Player left, Player right) {
      return -Integer.compare(left.oldPoints(), right.oldPoints());
    }
  };
  
  private static final Ordering<Player> byNewPoints = new Ordering<Player>() {
    @Override
    public int compare(Player left, Player right) {
      return -Integer.compare(left.newPoints(), right.newPoints());
    }
  };
}
