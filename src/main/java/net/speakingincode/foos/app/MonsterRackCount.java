package net.speakingincode.foos.app;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import net.speakingincode.foos.scrape.KToolPlay;
import net.speakingincode.foos.scrape.KToolResults;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class MonsterRackCount {
  private static final Logger log = Logger.getLogger(MonsterMatchesApp.class.getName());
  // Almost always two full racks.
  private static final double RACE_TO_SEVEN = 1.8;
  // Sometimes 3rd rack needed.
  private static final double TWO_OUT_OF_THREE = 2.3;

  private static void usageAndExit() {
    log.warning("Usage: netfoos-monster-rack-count input.ktool");
    System.exit(1);
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      usageAndExit();
    }
    String ktool = args[0];
    KToolResults results = KToolResults.fromJson(
        CharStreams.toString(new InputStreamReader(new FileInputStream(ktool), Charsets.UTF_8)));
    double racks = 0;
    for (KToolPlay play : results.plays()) {
      racks += estimateRacks(play);
      if (!play.matchWasPlayed()) {
        continue;
      }
      for (KToolPlay.Match match : play.disciplines()) {
        if (match.sets().size() == 1) {
          racks += RACE_TO_SEVEN;
        } else {
          racks += TWO_OUT_OF_THREE;
        }
      }
    }
    if (results.ko() != null) {
      for (KToolResults.Level ko : results.ko().levels()) {
        for (KToolPlay play : ko.plays()) {
          racks += estimateRacks(play);
        }
      }
      if (results.ko().third() != null) {
        for (KToolPlay play : results.ko().third().plays()) {
          racks += estimateRacks(play);
        }
      }
    }
    System.out.println(String.format("date\tplayers\tracks"));
    System.out.println(String.format("%s\t%d\t%.1f", results.created(), results.players().size(),
        racks));
  }

  private static double estimateRacks(KToolPlay play) {
    if (!play.matchWasPlayed()) {
      return 0;
    }
    double racks = 0;
    for (KToolPlay.Match match : play.disciplines()) {
      if (match.sets().size() == 1) {
        racks += RACE_TO_SEVEN;
      } else {
        racks += TWO_OUT_OF_THREE;
      }
    }
    return racks;
  }
}

