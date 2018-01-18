package net.speakingincode.foos.app;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.PlayerListChecker;
import net.speakingincode.foos.scrape.SingleMatchEvent;
import net.speakingincode.foos.scrape.Tournament;
import net.speakingincode.foos.scrape.TournamentEditor;

public class MonsterResultsApp {
  private static final Logger log = Logger.getLogger(MonsterResultsApp.class.getName());
  private static final Pattern infoPattern = Pattern.compile("(.*): (.*)");
  private static final Pattern resultPattern = Pattern.compile(
      "(?<w1>.*) and (?<w2>.*) (?<result>defeat|tie) (?<l1>.*) and (?<l2>[^;]*)(?:; k=(?<kValue>\\d+))?");
  private static final Credentials credentials = Credentials.load();
  
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      log.warning("Usage: netfoos-monster-results input.txt");
      System.exit(1);
    }
    WebDriver driver = new HtmlUnitDriver();
    MonsterResult result = parseInputFile(new File(args[0]));
    ImmutableSet<String> missing =
        new PlayerListChecker(credentials, driver).findMissingPlayers(result.players);
    if (!missing.isEmpty()) {
      log.warning("Missing some players, no results entered:\n" + 
          Joiner.on("\n").join(missing));
      System.exit(1);
    }
    TournamentEditor editor = new TournamentEditor(credentials, driver);
    String tournamentId = editor.create(result.tournament);
    log.info("Creating " + result.matches.size() + " matches.");
    List<SingleMatchEvent> matches = Lists.newArrayList();
    for (SingleMatchEvent.Builder b : result.matches) {
      matches.add(b.tournamentId(tournamentId).build());
    }
    
    summarizeResults(matches);
    
    int failCount = 0;
    for (SingleMatchEvent match : matches) {
      try {
        editor.createEvent(match);
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
        record(records, WinLoss.TIE, match.winnerPlayerTwo());
        record(records, WinLoss.TIE, match.loserPlayerOne());
        record(records, WinLoss.TIE, match.loserPlayerTwo());
      } else {
        record(records, WinLoss.WIN, match.winnerPlayerOne());
        record(records, WinLoss.WIN, match.winnerPlayerTwo());
        record(records, WinLoss.LOSS, match.loserPlayerOne());
        record(records, WinLoss.LOSS, match.loserPlayerTwo());
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

  private static MonsterResult parseInputFile(File in) throws IOException {
    List<String> lines = Files.readLines(in, Charsets.UTF_8);
    if (lines.size() < 6) {
      throw new IOException("Input file too short:\n" + Joiner.on('\n').join(lines));
    }
    Tournament.Builder tournament = Tournament.builder();
    int lineNum = 0;
    tournament.setName(parse("Name", lines.get(lineNum++)));
    tournament.setDescription(parse("Description", lines.get(lineNum++)));
    tournament.setDate(LocalDate.parse(parse("Date", lines.get(lineNum++))));
    tournament.setLocation(parse("Location", lines.get(lineNum++)));
    tournament.setAddress(parse("Address", lines.get(lineNum++)));
    tournament.setCity(parse("City", lines.get(lineNum++)));
    tournament.setState(parse("State", lines.get(lineNum++)));
    tournament.setZip(parse("Zip", lines.get(lineNum++)));
    String defaultKValue = parse("KValue", lines.get(lineNum++));
    int failCount = 0;
    ImmutableSet.Builder<String> players = ImmutableSet.builder();
    ImmutableList.Builder<SingleMatchEvent.Builder> matches = ImmutableList.builder();
    MonsterResult result = new MonsterResult();
    for (;lineNum < lines.size(); ++lineNum) {
      String line = lines.get(lineNum);
      line = line.trim();
      if (line.isEmpty()) {
        continue;
      }
      Matcher m = resultPattern.matcher(line);
      if (!m.matches()) {
        log.warning("Bad result format. Expected " + resultPattern.pattern() + ", got " + 
            line);
        ++failCount;
      }
      SingleMatchEvent.Builder match = SingleMatchEvent.builder();
      match.winnerPlayerOne(m.group("w1"));
      match.winnerPlayerTwo(m.group("w2"));
      match.loserPlayerOne(m.group("l1"));
      match.loserPlayerTwo(m.group("l2"));
      if ("tie".equals(m.group("result"))) {
        match.tie(true);
      }
      String kValue = m.group("kValue");
      if (kValue == null) {
        kValue = defaultKValue;
      }
      match.kValue(kValue.trim());
      for (String player : new String[] { "w1", "w2", "l1", "l2" }) {
        players.add(m.group(player));
      }
      matches.add(match);
    }
    if (failCount > 0) {
      throw new IOException("Failed to parse some result lines.");
    }
    result.tournament = tournament.build();
    result.players = players.build();
    result.matches = matches.build();
    return result;
  }

  private static String parse(String field, String line) throws IOException {
    Matcher m = infoPattern.matcher(line);
    if (!m.matches()) {
      throw new IOException("Expected pattern '" + infoPattern.pattern() + "', got " +
          line);
    }
    if (!m.group(1).equals(field)) {
      throw new IOException("Expected leading '" + field + "': got " + m.group(1));
    }
    return m.group(2);
  }

  private static class MonsterResult {
    Tournament tournament;
    ImmutableSet<String> players;
    ImmutableList<SingleMatchEvent.Builder> matches;
  }
}
