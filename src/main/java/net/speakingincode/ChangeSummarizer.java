package net.speakingincode;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Summarizes changes in player points and ranks.
 */
public class ChangeSummarizer {
  private static final int MAX_RANK_TO_SHOW = 100;
  private final ImmutableList<Player> playersByOldPoints;
  private final ImmutableList<Player> playersByNewPoints;

  public ChangeSummarizer(List<Player> players) {
    this.playersByOldPoints = Sorting.copySortedByOldPoints(players);
    this.playersByNewPoints = Sorting.copySortedByNewPoints(players);
  }
  
  public String getSummary() {
    ImmutableMap<Player, Integer> oldRanks = getPlayerToRank(playersByOldPoints, true);
    ImmutableMap<Player, Integer> newRanks = getPlayerToRank(playersByNewPoints, false);
    StringBuilder changeSummary = new StringBuilder();
    for (Player player : playersByNewPoints) {
      int newRank = newRanks.get(player);
      if (newRank > MAX_RANK_TO_SHOW) {
        break;
      }
      if (player.oldPoints() == player.newPoints()) {
        continue;
      }
      int delta = player.newPoints() - player.oldPoints();
      int oldRank = oldRanks.get(player);
      changeSummary.append(toStringRank(newRank));
      changeSummary.append(": ");
      changeSummary.append(player.name());
      changeSummary.append(": ");
      changeSummary.append(player.newPoints());
      changeSummary.append(" (");
      if (delta > 0) {
        changeSummary.append('+');
      }
      changeSummary.append(delta);
      if (oldRank != newRank) {
        changeSummary.append(", old rank: ");
        changeSummary.append(toStringRank(oldRank));
      }
      changeSummary.append(")\n");
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
}
