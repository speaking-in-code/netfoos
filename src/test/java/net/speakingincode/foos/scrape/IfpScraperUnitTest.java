package net.speakingincode.foos.scrape;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.speakingincode.foos.scrape.IfpScraper;

public class IfpScraperUnitTest {  
  @Test
  public void parseMensRecord() {
    assertEquals(6716, IfpScraper.parsePoints(
        "6656/6716 Singles/Doubles Points\nMASTER"));
  }
  
  @Test
  public void parseWomensRecord() {
    assertEquals(1500, IfpScraper.parsePoints(
        "1300/1500 Singles/Doubles Points\n" +
        "3430/3654 Women's Singles/Doubles Points\n" +
        "PRO"));
  }
}
