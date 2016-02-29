package net.speakingincode.foos.scrape;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Summarizes changes in player points and ranks.
 */
public class ChangeSummarizer {
  private static final int MAX_RANK_TO_SHOW = 100;
  private final ImmutableList<Player> playersByOldPoints;
  private final ImmutableList<Player> playersByNewPoints;
  private final ImmutableMap<Player, Integer> oldRanks;
  private final ImmutableMap<Player, Integer> newRanks;

  public ChangeSummarizer(List<Player> players) {
    playersByOldPoints = Sorting.copySortedByOldPoints(players);
    playersByNewPoints = Sorting.copySortedByNewPoints(players);
    oldRanks = getPlayerToRank(playersByOldPoints, true);
    newRanks = getPlayerToRank(playersByNewPoints, false);
  }
  
  /**
   * Gets a summary for all players with points changes.
   */
  public String getChangedPlayerSummary() {
    StringBuilder changeSummary = new StringBuilder();
    for (Player player : playersByNewPoints) {
      if (player.oldPoints() == player.newPoints()) {
        continue;
      }
      changeSummary.append(getPlayerChange(player));
    }
    return changeSummary.toString();
  }
  
  /**
   * Gets a summary for the top 100 players with points changes.
   */
  public String getTopPlayerSummary() {
    StringBuilder changeSummary = new StringBuilder();
    for (Player player : playersByNewPoints) {
      int newRank = newRanks.get(player);
      if (newRank > MAX_RANK_TO_SHOW) {
        break;
      }
      if (player.oldPoints() == player.newPoints()) {
        continue;
      }
      changeSummary.append(getPlayerChange(player));
    }
    return changeSummary.toString();
  }
  
  private String getPlayerChange(Player player) {
    int newRank = newRanks.get(player);
    StringBuilder changeSummary = new StringBuilder();
    int delta = player.newPoints() - player.oldPoints();
    int oldRank = oldRanks.get(player);
    changeSummary.append(RankStrings.toStringRank(newRank));
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
      changeSummary.append(RankStrings.toStringRank(oldRank));
    }
    changeSummary.append(")\n");
    return changeSummary.toString();
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
