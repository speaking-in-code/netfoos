package net.speakingincode.foos.app;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.speakingincode.foos.scrape.CloseableWebDriver;
import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.IfpPoints;
import net.speakingincode.foos.scrape.NetfoosTable;
import net.speakingincode.foos.scrape.NetfoosUpdater;
import net.speakingincode.foos.scrape.Player;
import net.speakingincode.foos.scrape.PlayerListChecker;
import net.speakingincode.foos.scrape.PointsBook;
import net.speakingincode.foos.scrape.PointsBookPlayer;
import net.speakingincode.foos.scrape.PointsUpdater.Mode;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class NetfoosBaseUpdateApp {
  private static boolean update = false;

  public static void main(String[] args) throws IOException {
    for (String arg : args) {
      if ("--update".equals(arg)) {
        update = true;
      } else {
        throw new IOException("Unknown argument: " + arg + ". Only supported arg is --update");
      }
    }
    PointsBook currentSpreadsheet = PointsBook.load();
    IfpPoints currentIfp = IfpPoints.load();
    NetfoosTable currentNetfoos = readNetfoosBasePoints();
    validateNameMappings(currentSpreadsheet, currentIfp, currentNetfoos);
    syncIfpToNetfoos(currentSpreadsheet, currentIfp, currentNetfoos);
    System.exit(0);
  }

  private static void validateNameMappings(PointsBook currentSpreadsheet, IfpPoints ifpTable,
      NetfoosTable netfoosTable) throws IOException {
    List<String> mismatches = Lists.newArrayList();
    for (PointsBookPlayer bookPlayer : currentSpreadsheet.getPlayers()) {
      if (bookPlayer.ifpId() == null || bookPlayer.ifpId().isEmpty()) {
        continue;
      }
      Integer ifpPoints = ifpTable.getPoints(bookPlayer.ifpId());
      if (ifpPoints == null) {
        mismatches.add("spreadsheet to IFP id: " + bookPlayer.ifpId());
      }
      if (netfoosTable.get(bookPlayer.name()) == null) {
        mismatches.add("spreadsheet to netfoos: " + bookPlayer.name());
      }
    }
    if (mismatches.isEmpty()) {
      return;
    }
    throw new IOException("Mismatched IFP names:\n" + Joiner.on("\n").join(mismatches));
  }

  private static NetfoosTable readNetfoosBasePoints() throws IOException {
    try (CloseableWebDriver driver = new CloseableWebDriver(new HtmlUnitDriver())) {
      PlayerListChecker checker = new PlayerListChecker(Credentials.load(), driver.getDriver());
      return checker.loadNetfoosPlayers();
    }
  }

  private static void syncIfpToNetfoos(PointsBook currentSpreadsheet, IfpPoints ifpTable,
      NetfoosTable currentNetfoos) {
      ImmutableList.Builder<Player> updates = ImmutableList.builder();
      for (PointsBookPlayer bookPlayer : currentSpreadsheet.getPlayers()) {
        Player currentLocal = currentNetfoos.get(bookPlayer.name());
        Integer currentIfp = ifpTable.getPoints(bookPlayer.ifpId());
        if (currentIfp != null && currentLocal.oldBasePoints() != currentIfp) {
          System.out.println(String.format("Will update %s (%s) base points from %d to %d", bookPlayer.name(), bookPlayer.ifpId(),
              currentLocal.oldBasePoints(), currentIfp));
          updates.add(currentLocal.toBuilder().newBasePoints(currentIfp).build());
        }
      }
      if (update) {
        new NetfoosUpdater(Credentials.load(), Mode.BASE).runUpdates(updates.build());
      } else {
        System.out.println("Run with --update to apply changes.");
      }
  }
}
