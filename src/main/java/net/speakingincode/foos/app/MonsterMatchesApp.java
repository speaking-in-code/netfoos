package net.speakingincode.foos.app;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import net.speakingincode.foos.scrape.KToolPlay;
import net.speakingincode.foos.scrape.KToolPlayer;
import net.speakingincode.foos.scrape.KToolResults;
import net.speakingincode.foos.scrape.KToolTeam;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonsterMatchesApp {
  private static final Logger log = Logger.getLogger(MonsterMatchesApp.class.getName());
  private static final Map<String, String> playerNames = Maps.newHashMap();
  private static final Map<String, String> teamNames = Maps.newHashMap();

  private static void usageAndExit() {
    log.warning("Usage: netfoos-monster-matches input.ktool");
    System.exit(1);
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      usageAndExit();
    }
    String ktool = args[0];
    KToolResults results = KToolResults.fromJson(
        CharStreams.toString(new InputStreamReader(new FileInputStream(ktool), Charsets.UTF_8)));
    for (KToolPlayer player : results.players()) {
      playerNames.put(player.id(), player.name());
    }
    for (KToolTeam team : results.teams()) {
      String teamName = playerName(team.players().get(0).id());
      if (team.players().size() > 1) {
        teamName += " & " + playerName(team.players().get(1).id());
      }
      teamNames.put(team.id(), teamName);
    }
    System.out.println("Seeding rounds");
    for (KToolPlay play : results.plays()) {
      System.out.println("  " + match(play));
    }
    System.out.println("");
    for (KToolResults.Level ko : results.ko().levels()) {
      System.out.println(roundName(ko.name()));
      for (KToolPlay play : ko.plays()) {
        System.out.println("  " + match(play));
      }
      System.out.println("");
    }
    if (results.ko().third() != null) {
      System.out.println(roundName(results.ko().third().name()));
      for (KToolPlay play : results.ko().third().plays()) {
        System.out.println("  " + match(play));
      }
    }
  }

  private static final Pattern PATTERN = Pattern.compile("1/(\\d+)");

  private static String roundName(String name) {
    if ("THIRD".equals(name)) {
      return "3rd place";
    }
    if ("1/1".equals(name)) {
      return "Finals";
    }
    if ("1/2".equals(name)) {
      return "Semi-finals";
    }
    Matcher m = PATTERN.matcher(name);
    if (m.matches()) {
      return "Round of " + Integer.parseInt(m.group(1))*2;
    }
    return name;
  }

  private static String playerName(String id) {
    String name = playerNames.get(id);
    return name != null ? name : "";
  }

  private static String match(KToolPlay play) {
    String t1 = teamNames.get(play.team1().id());
    String t2 = teamNames.get(play.team2().id());
    String result = t1 + " vs " + t2;
    if (!play.matchWasPlayed()) {
      return result + ": no result";
    }
    KToolPlay.Match match = play.disciplines().get(0);
    KToolPlay.Set set = match.sets().get(0);
    if (set.team1() > set.team2()) {
      return result + ": " + t1;
    }
    if (set.team1() < set.team2()) {
      return result + ": " + t2;
    }
    return result + ": tie";
  }
}
