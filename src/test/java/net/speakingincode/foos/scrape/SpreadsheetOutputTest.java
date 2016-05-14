package net.speakingincode.foos.scrape;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class SpreadsheetOutputTest {
  @Test
  public void emptyInput() {
    assertEquals("Last\tFirst\tPoints\n",
        new SpreadsheetOutput(ImmutableList.<Player>of()).getOutput());
  }
  
  @Test
  public void onePlayer() {
    assertEquals(
        "Last\tFirst\tPoints\n" +
        "Alpha\tAlice\t500\n",
        new SpreadsheetOutput(ImmutableList.of(
            emptyBuilder().name("Alpha, Alice").oldPoints(0).newPoints(500).build()
        )).getOutput());
  }
  
  @Test
  public void twoPlayers() {
    assertEquals(
        "Last\tFirst\tPoints\n" +
        "Alpha\tAlice\t500\n" +
        "Beta\tBob\t499\n",
        new SpreadsheetOutput(ImmutableList.of(
            emptyBuilder().name("Alpha, Alice").oldPoints(0).newPoints(500).build(),
            emptyBuilder().name("Beta, Bob").oldPoints(0).newPoints(499).build()
        )).getOutput());
  }
  
  // This test modifies production data at the moment. I should move it to to a test
  // spreadsheet.
  /*
  @Test
  public void publicSheetUpdate() throws IOException {
    new SpreadsheetOutput(ImmutableList.of(
        emptyBuilder().name("Alpha, Alice").oldPoints(0).newPoints(500).build(),
        emptyBuilder().name("Beta, Bob").oldPoints(0).newPoints(499).build()
    )).publishToGoogleSheets();
  }
  */
  
  private Player.Builder emptyBuilder() {
    return Player.builder().newBasePoints(0).oldBasePoints(0);
  }
}
