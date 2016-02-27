package net.speakingincode.foos.scrape;

import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public class SpreadsheetOutput {
  private static final Splitter nameSplitter = Splitter.on(',').trimResults();
  private final ImmutableList<Player> byPoints;
  
  public SpreadsheetOutput(List<Player> player) {
    byPoints = Sorting.copySortedByNewPoints(player);
  }
  
  public String getOutput() {
    StringBuilder out = new StringBuilder();
    out.append("Last\tFirst\tPoints\n");
    for (Player player : byPoints) {
      List<String> name = nameSplitter.splitToList(player.name());
      out.append(name.get(0));
      out.append('\t');
      out.append(name.get(1));
      out.append('\t');
      out.append(player.newPoints());
      out.append('\n');
    }
    return out.toString();
  }
}
