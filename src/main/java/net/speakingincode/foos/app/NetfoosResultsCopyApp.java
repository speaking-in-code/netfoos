package net.speakingincode.foos.app;

import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.TournamentScraper;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class NetfoosResultsCopyApp {
  public static void main(String args[]) {
    TournamentScraper scraper = new TournamentScraper(new HtmlUnitDriver(), Credentials.load());

  }
}
