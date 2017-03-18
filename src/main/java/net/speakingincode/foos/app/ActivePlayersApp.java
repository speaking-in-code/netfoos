package net.speakingincode.foos.app;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.TournamentResults;
import net.speakingincode.foos.scrape.TournamentResults.EventResults;
import net.speakingincode.foos.scrape.TournamentResults.Finish;
import net.speakingincode.foos.scrape.TournamentScraper;

public class ActivePlayersApp {
  public static void main(String[] args) throws IOException {
    TournamentScraper scraper = new TournamentScraper(new HtmlUnitDriver(), Credentials.load());
    ImmutableList<TournamentResults> results = scraper.getAllResults();
    Multimap<String, TournamentResults> byYear = ArrayListMultimap.create();
    for (TournamentResults result : results) {
      if (result.events().isEmpty()) {
        continue;
      }
      String year = result.events().get(0).date().split("-")[0];
      byYear.put(year, result);
    }
    List<String> years = Lists.newArrayList(byYear.keySet().iterator());
    years.sort(null);
    for (String year : years) {
      System.out.println("Frequent players: " + year);
      printFrequentPlayers(byYear.get(year));
      System.out.println();
    }
  }

  static class MutableInt {
    int val;
  }
  
  private static void printFrequentPlayers(Collection<TournamentResults> results) {
    Map<String, MutableInt> playerEventCount = Maps.newHashMap();
    for (TournamentResults result : results) {
      for (EventResults event : result.events()) {
        for (Finish finish : event.finishes()) {
          increment(playerEventCount, finish.playerOne());
          increment(playerEventCount, finish.playerTwo());
        }
      }
    }
    List<Map.Entry<String, MutableInt>> descending = Lists.newArrayList(
        playerEventCount.entrySet().iterator());
    descending.sort(descendingFrequency);
    for (Map.Entry<String, MutableInt> player : descending) {
      System.out.println(String.format("  %s: %d", player.getKey(),
          player.getValue().val));
    }
  }
    
  private static Comparator<Map.Entry<String, MutableInt>> descendingFrequency =
      new Comparator<Map.Entry<String, MutableInt>>() {
        @Override
        public int compare(Entry<String, MutableInt> o1, Entry<String, MutableInt> o2) {
          return o2.getValue().val - o1.getValue().val;
        } 
  };

  private static void increment(Map<String, MutableInt> playerEventCount, String player) {
    MutableInt counter = playerEventCount.get(player);
    if (counter == null) {
      counter = new MutableInt();
      counter.val = 1;
      playerEventCount.put(player, counter);
    } else {
      counter.val++;
    }
  }
}
