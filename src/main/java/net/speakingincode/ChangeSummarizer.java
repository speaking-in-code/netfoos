package net.speakingincode;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;

/**
 * Summarizes changes in player points and ranks.
 */
public class ChangeSummarizer {
  private final ImmutableList<Player> playersByOldPoints;
  private final ImmutableList<Player> playersByNewPoints;

  public ChangeSummarizer(List<Player> players) {
    this.playersByOldPoints = byOldPoints.immutableSortedCopy(players);
    this.playersByNewPoints = byNewPoints.immutableSortedCopy(players);
  }
  
  public String getSummary() {
    ImmutableMap<Player, Integer> oldRanks = getPlayerToRank(playersByOldPoints, true);
    ImmutableMap<Player, Integer> newRanks = getPlayerToRank(playersByNewPoints, false);
    StringBuilder changeSummary = new StringBuilder();
    for (Player player : playersByNewPoints) {
      if (player.oldPoints() == player.newPoints()) {
        continue;
      }
      int delta = player.newPoints() - player.oldPoints();
      int oldRank = oldRanks.get(player);
      int newRank = newRanks.get(player);
      changeSummary.append(player.name());
      changeSummary.append(": ");
      changeSummary.append(player.newPoints());
      changeSummary.append(" (");
      if (delta > 0) {
        changeSummary.append('+');
      }
      changeSummary.append(delta);
      changeSummary.append(")");
      if (oldRank == newRank) {
        changeSummary.append(". Rank: " + toStringRank(newRank));
      } else {
        changeSummary.append(". New rank: " );
        changeSummary.append(toStringRank(newRank));
        changeSummary.append(", was ");
        changeSummary.append(toStringRank(oldRank));
      }
      changeSummary.append(".\n");
    }
    return changeSummary.toString();
  }
  
  @VisibleForTesting
  static String toStringRank(int rank) {
    ++rank; // human readable, start index at 1.
    StringBuilder rankStr = new StringBuilder();
    rankStr.append(rank);
    if (rank >= 11 && rank <= 13) {
      // Special case for the teens.
      rankStr.append("th");
    } else {
      switch (rank % 10) {
      case 1:
        rankStr.append("st");
        break;
      case 2:
        rankStr.append("nd");
        break;
      case 3:
        rankStr.append("rd");
        break;
      default:
        rankStr.append("th");
        break;
      }
    }
    return rankStr.toString();
  }
  
  private ImmutableMap<Player, Integer> getPlayerToRank(ImmutableList<Player> sorted, boolean old) {
    if (sorted.isEmpty()) {
      return ImmutableMap.<Player, Integer>of();
    }
    // Note handling of ties:
    // A: 800 -> 1st
    // B: 800 -> 1st
    // C: 700 -> 3rd
    int rank = 0;
    ImmutableMap.Builder<Player, Integer> ranked = ImmutableMap.builder();
    ranked.put(sorted.get(0), rank);
    for (int i = 1; i < sorted.size(); ++i) {
      Player prev = sorted.get(i-1);
      Player cur = sorted.get(i);
      if (old) {
        if (prev.oldPoints() != cur.oldPoints()) {
          rank = i;
        }
      } else {
        if (prev.newPoints() != cur.newPoints()) {
          rank = i;
        }
      }
      ranked.put(sorted.get(i), rank);
    }
    return ranked.build();
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
