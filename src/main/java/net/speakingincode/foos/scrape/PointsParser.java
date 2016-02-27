package net.speakingincode.foos.scrape;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * NAME              BASE CURRENT UPDATED CHANGE HI/LOW
   Spredeman, Tony   7846 7846    7846    0      7878/7846
   Da Rosa, Fernando 6820 6692    6692    0      6820/6692
   Loffredo, Todd    6721 6721    6676    -45    6721/6657
   Pipkin, Jeff      3809 No Data 3777    3777   3809/3777
 *
 */
public class PointsParser {
  private static final Splitter splitter = Splitter.on(' ').trimResults().omitEmptyStrings();
  
  private static class State {
    private int cur = 0;
    private List<String> fields;
    
    public State(List<String> fields) {
      this.fields = fields;
    }
    
    public String parseName() {
      List<String> nameFields = Lists.newArrayList();
      // Find end of first name. Could be "No Data", or could be a number.
      for (; cur < fields.size(); ++cur) {
        if (atNoData()) {
          break;
        }
        if (fields.get(cur).matches("\\d+")) {
          break;
        }
        nameFields.add(fields.get(cur));
      }
      if (cur == fields.size() || nameFields.isEmpty()) {
        return null;
      }
      return Joiner.on(' ').join(nameFields);
    }
    
    private boolean atNoData() {
      return (cur + 1) < fields.size() &&
          fields.get(cur).equals("No") &&
          fields.get(cur + 1).equals("Data");
    }
    
    public int parsePoints() {
      if (atNoData()) {
        cur += 2;
        return 0;
      }
      String points = fields.get(cur);
      ++cur;
      return Integer.parseInt(points);
    }
  }
  
  public Player parse(String input) {
    Player.Builder b = Player.builder();
    State state = new State(splitter.splitToList(input));
    String name = state.parseName();
    if (name == null) {
      return null;
    }
    b.name(name);
    // Skip base.
    state.parsePoints();
    b.oldPoints(state.parsePoints());
    b.newPoints(state.parsePoints());
    return b.build();
  }
}
