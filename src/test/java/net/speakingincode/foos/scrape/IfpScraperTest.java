package net.speakingincode.foos.scrape;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class IfpScraperTest {
  private static IfpScraper scraper;

  @BeforeClass
  public static void before() throws Exception {
    ChromeDriverManager.chromedriver().setup();
    scraper = new IfpScraper();
  }

  @AfterClass
  public static void after() throws Exception {
    scraper.shutdown();
  }

  @Test
  public void testScrapeTony() throws Exception {
    int points = scraper.scrapePoints("Tony SPREDEMAN");
    assertThat(points, greaterThan(5000));
  }

  @Test
  public void testScrapeMelissa() throws Exception {
    int points = scraper.scrapePoints("Melissa Kegg");
    assertThat(points, greaterThan(1000));
  }

  @Test
  public void testScrapeAmbiguousNameWithState() throws Exception {
    int points = scraper.scrapePoints("Paul Richards (CA)");
    assertThat(points, greaterThan(1000));
  }

  @Test
  public void testAmbiguousName() throws Exception {
    assertEquals(0, scraper.scrapePoints("A B"));
  }

  @Test
  public void testNoMatchingName() throws Exception {
    assertEquals(0, scraper.scrapePoints("ABBBA ZXXXL"));
  }
}
