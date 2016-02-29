package net.speakingincode.foos.scrape;

import static org.junit.Assert.*;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class TournamentScraperTest {
  private TournamentScraper scraper;
  
  @Before
  public void before() throws Exception {
    scraper = new TournamentScraper(new HtmlUnitDriver(), Credentials.load());
  }
  
  @Test
  public void testScrape() throws Exception {
    TournamentResults result = scraper.getRecentResults();
    // Tough to test any actual values here, since they change frequently.
    assertNotNull(result);
    assertThat(result.events().size(), Matchers.greaterThan(0));
  }
}
