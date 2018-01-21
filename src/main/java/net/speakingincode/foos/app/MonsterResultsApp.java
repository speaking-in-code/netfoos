package net.speakingincode.foos.app;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import net.speakingincode.foos.scrape.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonsterResultsApp {
  private static final Logger log = Logger.getLogger(MonsterResultsApp.class.getName());
  private static final Credentials credentials = Credentials.load();
  
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      log.warning("Usage: netfoos-monster-results input.txt");
      System.exit(1);
    }
    WebDriver driver = new HtmlUnitDriver();
    List<String> lines = Files.readLines(new File(args[0]), Charsets.UTF_8);
    MonsterResult result = MonsterResultsFile.load(lines);
    ImmutableSet<String> missing =
        new PlayerListChecker(credentials, driver).findMissingPlayers(result.players());
    if (!missing.isEmpty()) {
      log.warning("Missing some players, no results entered:\n" + 
          Joiner.on("\n").join(missing));
      System.exit(1);
    }
    TournamentEditor editor = new TournamentEditor(credentials, driver);
    String tournamentId = editor.create(result.tournament());
    log.info("Creating " + result.matches().size() + " matches.");

    summarizeResults(result.matches());
    
    int failCount = 0;
    int matchCount = 1;
    for (SingleMatchEvent match : result.matches()) {
      try {
        editor.createEvent(tournamentId, matchCount++, match);
      } catch (IOException e) {
        log.log(Level.WARNING, "Failed to create event " + match, e);
        ++failCount;
      }
    }
    if (failCount > 0) {
      log.warning("Encountered " + failCount + " errors.");
    }
  }

  static class Record {
    int wins = 0;
    int ties = 0;
    int losses = 0;
    
    public float getAveragePoints() {
      return ((float) (2 * wins + ties)) / ((float) (wins + ties + losses));
    }
  }
  
  static enum WinLoss {
    WIN,
    TIE,
    LOSS
  }
  
  private static void summarizeResults(List<SingleMatchEvent> matches) {
    final Map<String, Record> records = Maps.newHashMap();
    for (SingleMatchEvent match : matches) {
      if (match.tie()) {
        record(records, WinLoss.TIE, match.winnerPlayerOne());
        if (match.winnerPlayerTwo() != null) {
          record(records, WinLoss.TIE, match.winnerPlayerTwo());
        }
        record(records, WinLoss.TIE, match.loserPlayerOne());
        if (match.loserPlayerTwo() != null) {
          record(records, WinLoss.TIE, match.loserPlayerTwo());
        }
      } else {
        record(records, WinLoss.WIN, match.winnerPlayerOne());
        if (match.winnerPlayerTwo() != null) {
          record(records, WinLoss.WIN, match.winnerPlayerTwo());
        }
        record(records, WinLoss.LOSS, match.loserPlayerOne());
        if (match.loserPlayerTwo() != null) {
          record(records, WinLoss.LOSS, match.loserPlayerTwo());
        }
      }
    }
    List<String> sorted = Lists.newArrayList(records.keySet());
    sorted.sort(new Comparator<String>() {
      @Override
      public int compare(String p1, String p2) {
        Record r1 = records.get(p1);
        Record r2 = records.get(p2);
        float delta = r1.getAveragePoints() - r2.getAveragePoints();
        if (delta < 0) {
          return +1;
        } else if (delta > 0) {
          return -1;
        } else {
          return 0;
        }
      }
    });
    StringBuilder result = new StringBuilder();
    result.append("Per Player Results: wins-ties-losses matches\n");
    for (String player : sorted) {
      Record record = records.get(player);
      result.append(String.format("  %s: %d-%d-%d\n", player,
          record.wins, record.ties, record.losses));
    }
    log.info("\n" + result.toString());
  }

  private static void record(Map<String, Record> records, WinLoss winLoss, String player) {
    Record record = records.get(player);
    if (record == null) {
      record = new Record();
      records.put(player, record);
    }
    if (winLoss == WinLoss.WIN) {
      record.wins++;
    } else if (winLoss == WinLoss.TIE) {
      record.ties++;
    } else {
      record.losses++;
    }
  }
}
