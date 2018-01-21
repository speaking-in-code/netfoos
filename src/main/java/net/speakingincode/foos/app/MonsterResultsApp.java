package net.speakingincode.foos.app;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.MonsterResult;
import net.speakingincode.foos.scrape.MonsterResultsFile;
import net.speakingincode.foos.scrape.PlayerListChecker;
import net.speakingincode.foos.scrape.SingleMatchEvent;
import net.speakingincode.foos.scrape.Tournament;
import net.speakingincode.foos.scrape.TournamentEditor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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

    summarizeResults(result.tournament(), result.matches());
    
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

  public MonsterResultsApp() {
  }

  private static void summarizeResults(Tournament tournament, List<SingleMatchEvent> matches) {
    final Map<String, Record> records = Maps.newHashMap();
    for (SingleMatchEvent match : matches) {
      String w1;
      String w2;
      String l1;
      String l2;
      switch (tournament.outputFormat()) {
        case TEAM:
          w1 = match.winnerPlayerOne() + " & " + match.winnerPlayerTwo();
          w2 = null;
          l1 = match.loserPlayerOne() + " & " + match.loserPlayerTwo();
          l2 = null;
          break;
        case INDIVIDUAL:
          w1 = match.winnerPlayerOne();
          w2 = match.winnerPlayerTwo();
          l1 = match.loserPlayerOne();
          l2 = match.loserPlayerTwo();
          break;
        default:
          throw new RuntimeException("Unknown output format type.");
      }
      if (match.tie()) {
        record(records, WinLoss.TIE, w1);
        if (w2 != null) {
          record(records, WinLoss.TIE, w2);
        }
        record(records, WinLoss.TIE, l1);
        if (l2 != null) {
          record(records, WinLoss.TIE, l2);
        }
      } else {
        record(records, WinLoss.WIN, w1);
        if (w2 != null) {
          record(records, WinLoss.WIN, w2);
        }
        record(records, WinLoss.LOSS, l1);
        if (l2 != null) {
          record(records, WinLoss.LOSS, l2);
        }
      }
    }

    List<String> sorted = Lists.newArrayList(records.keySet());
    Collections.sort(sorted, Comparator.comparingDouble(key -> {
      return records.get(key).getAveragePoints();
    }).thenComparingInt(key -> {
      return records.get(key).wins;
    }).reversed());

    StringBuilder result = new StringBuilder();
    switch (tournament.outputFormat()) {
      case TEAM:
        result.append("Team Results: wins-ties-losses matches\n");
        break;
      case INDIVIDUAL:
        result.append("Player Results: wins-ties-losses matches\n");
        break;
    }
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
