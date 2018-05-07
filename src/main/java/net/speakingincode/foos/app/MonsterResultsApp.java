package net.speakingincode.foos.app;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.ResultsParser;
import net.speakingincode.foos.scrape.ResultsParserConfig;
import net.speakingincode.foos.scrape.MonsterResult;
import net.speakingincode.foos.scrape.NameMap;
import net.speakingincode.foos.scrape.PlayerListChecker;
import net.speakingincode.foos.scrape.RankStrings;
import net.speakingincode.foos.scrape.SingleMatchEvent;
import net.speakingincode.foos.scrape.TournamentEditor;
import net.speakingincode.foos.scrape.TournamentResults;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.File;
import java.io.FileInputStream;
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
  private static final NameMap nameMap = NameMap.load();

  private static void usageAndExit() {
    log.warning("Usage: netfoos-monster-results (input.ktool location.tournament)|(event.matches)");
    System.exit(1);
  }

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      usageAndExit();
    }
    WebDriver driver = new HtmlUnitDriver();
    MonsterResult shortNames = null;
    ResultsParserConfig.Builder config = ResultsParserConfig.builder();
    for (String arg : args) {
      if (arg.endsWith(".ktool")) {
        config.ktool(new FileInputStream(arg));
      } else if (arg.endsWith(".tournament")) {
        config.metadata(new FileInputStream(arg));
      } else if (arg.endsWith(".matches")) {
        config.matches(new File(arg));
      } else {
        usageAndExit();
      }
    }
    shortNames = ResultsParser.load(config.build());

    MonsterResult fullNames = transformToFullNames(shortNames);
    ImmutableSet<String> missing =
        new PlayerListChecker(credentials, driver).findMissingPlayers(fullNames.players());
    if (!missing.isEmpty()) {
      log.warning("Missing some players, no results entered:\n" + 
          Joiner.on("\n").join(missing));
      System.exit(1);
    }

    summarizeResults(shortNames);
    TournamentEditor editor = new TournamentEditor(credentials, driver);
    String tournamentId = editor.create(fullNames.tournament());
    log.info("Creating " + fullNames.matches().size() + " matches.");

    int failCount = 0;
    int matchCount = 1;
    for (SingleMatchEvent match : fullNames.matches()) {
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

  private static void summarizeResults(MonsterResult monsterResult) {
    final Map<String, Record> records = Maps.newHashMap();
    for (SingleMatchEvent match : monsterResult.matches()) {
      String w1;
      String w2;
      String l1;
      String l2;
      switch (monsterResult.tournament().outputFormat()) {
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
    if (monsterResult.finishes() != null && !monsterResult.finishes().isEmpty()) {
      result.append("Playoff Results\n");
      for (TournamentResults.Finish f : monsterResult.finishes()) {
        result.append(RankStrings.toStringRank(f.finish()));
        result.append(": ");
        result.append(f.playerOne());
        if (f.playerTwo() != null) {
          result.append(" & ");
          result.append(f.playerTwo());
        }
        result.append("\n");
      }
    }
    switch (monsterResult.tournament().outputFormat()) {
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

  private static MonsterResult transformToFullNames(MonsterResult orig) {
    MonsterResult.Builder b = orig.toBuilder();
    ImmutableSet.Builder<String> players = ImmutableSet.builder();
    for (String player : orig.players()) {
      String renamed = nameMap.fullName(player);
      players.add(renamed);
    }
    b.players(players.build());
    ImmutableList.Builder<SingleMatchEvent> matches = ImmutableList.builder();
    for (SingleMatchEvent e : orig.matches()) {
        SingleMatchEvent.Builder renamed = e.toBuilder();
        renamed.winnerPlayerOne(nameMap.fullName(e.winnerPlayerOne()));
        renamed.winnerPlayerTwo(nameMap.fullName(e.winnerPlayerTwo()));
        renamed.loserPlayerOne(nameMap.fullName(e.loserPlayerOne()));
        renamed.loserPlayerTwo(nameMap.fullName(e.loserPlayerTwo()));
        matches.add(renamed.build());
    }
    b.matches(matches.build());
    return b.build();
  }
}
