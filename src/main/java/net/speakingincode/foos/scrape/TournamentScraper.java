package net.speakingincode.foos.scrape;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import net.speakingincode.foos.scrape.TournamentResults.EventResults;
import net.speakingincode.foos.scrape.TournamentResults.Finish;

public class TournamentScraper {
  private static final Splitter lineSplitter = Splitter.on('\n').trimResults().omitEmptyStrings();
  private static final Splitter tabSplitter = Splitter.on('\t');
  private final WebDriver driver;
  private final Credentials credentials;

  public TournamentScraper(WebDriver driver, Credentials credentials) {
    this.driver = driver;
    this.credentials = credentials;
  }
  
  public TournamentResults getRecentResults() throws IOException {
    new NetfoosLogin(credentials, driver).login();
    driver.findElement(By.linkText("Admin Modules")).click();
    checkPageContains("Admin Modules", "NetFoos Admin Modules");
    driver.findElement(By.linkText("ITSF_Results_Export_1_0")).click();
    checkPageContains("Results Export Tournament List", "ITSF Results Export 1.0");
    // TODO: figure out how to make selecting certain dates configurable.
    /*
    Select tournamentList = new Select(driver.findElement(By.name("nfts_mod_1")));
    tournamentList.selectByVisibleText("Tuesday DYP (2016-03-29)");
    */
    driver.findElement(By.tagName("form")).submit();
    checkPageContains("Results Export Event List", "Click Event to Export");
    List<String> resultLinks = Lists.newArrayList();
    for (WebElement link : driver.findElements(By.tagName("a"))) {
      String dest = link.getAttribute("href");
      if (dest.contains("action=ITSF_Results_Export_1_0") && dest.contains("nfts_mod_1")) {
        resultLinks.add(dest);
      }
    }
    TournamentResults.Builder tournament = TournamentResults.builder();
    for (String resultLink : resultLinks) {
      driver.get(resultLink);
      EventResults eventResults = parseResults(resultLink, driver.getPageSource());
      tournament.addEvent(eventResults);
    }
    return tournament.build();
  }
  
  @VisibleForTesting
  static EventResults parseResults(String resultLink, String results) throws IOException {
    ParsedUrl url = ParsedUrl.parse(resultLink);
    String account = url.getQueryArgs().get("account").iterator().next();
    String eventId = url.getQueryArgs().get("nfts_mod_2").iterator().next();
    EventResults.Builder event = EventResults.builder();
    List<String> lines = lineSplitter.splitToList(results);
    event.tournamentName(lines.get(0));
    event.date(lines.get(1));
    event.eventName(lines.get(2));
    event.chartUrl(String.format(
        "http://www.netfoos.com/display/%s/charts/1/%s.html", account, eventId));
    if (!lines.get(3).startsWith("Finish\tLast Name")) {
      throw new IOException("Couldn't parse results: " + results);
    }
    for (int i = 4; i < lines.size(); ++i) {
      List<String> fields = tabSplitter.splitToList(lines.get(i));
      int rank = Integer.parseInt(fields.get(0));
      String playerOne = getPlayerOne(fields);
      String playerTwo = getPlayerTwo(fields);

      event.addFinish(Finish.builder()
          .finish(rank)
          .playerOne(playerOne)
          .playerTwo(playerTwo)
          .build());
    }
    return event.build();
  }
  
  private static String getPlayerOne(List<String> fields) {
    return formatName(fields.get(2), fields.get(1));
  }
  
  private static String getPlayerTwo(List<String> fields) {
    if (fields.size() > 4) {
      return formatName(fields.get(5), fields.get(4));
    }
    return null;
  }
  
  private static String formatName(String first, String last) {
    return first + " " + last.charAt(0);
  }
  
  private void checkPageContains(String name, String contents) throws IOException {
    if (!driver.getPageSource().contains(contents)) {
      throw new IOException("Could not load " + name + ": " + driver.getPageSource());
    }
  }
}
