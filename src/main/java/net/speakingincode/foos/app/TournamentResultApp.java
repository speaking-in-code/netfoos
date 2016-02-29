package net.speakingincode.foos.app;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.RankStrings;
import net.speakingincode.foos.scrape.TournamentResults;
import net.speakingincode.foos.scrape.TournamentResults.EventResults;
import net.speakingincode.foos.scrape.TournamentResults.Finish;
import net.speakingincode.foos.scrape.TournamentScraper;

public class TournamentResultApp {
  public static void main(String[] args) throws IOException {
    TournamentScraper scraper = new TournamentScraper(new HtmlUnitDriver(), Credentials.load());
    TournamentResults results = scraper.getRecentResults();
    List<String> out = Lists.newArrayList();
    out.add("Results from " + results.events().get(0).tournamentName() + ":");
    out.add("");
    for (EventResults event : results.events()) {
      out.add("-- " + event.eventName() + " --");
      out.add("Chart: " + event.chartUrl());
      for (Finish finish : event.finishes()) {
        StringBuilder line = new StringBuilder();
        line.append(RankStrings.toStringRank(finish.finish() - 1));
        line.append(": ");
        line.append(finish.playerOne());
        if (finish.playerTwo() != null) {
          line.append(", ");
          line.append(finish.playerTwo());
        }
        out.add(line.toString());
      }
      out.add("");
    }
    System.out.println(Joiner.on('\n').join(out));
  }
}
