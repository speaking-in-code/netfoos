package net.speakingincode;

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
            Player.builder().name("Alpha, Alice").oldPoints(0).newPoints(500).build()
        )).getOutput());
  }
  
  @Test
  public void twoPlayers() {
    assertEquals(
        "Last\tFirst\tPoints\n" +
        "Alpha\tAlice\t500\n" +
        "Beta\tBob\t499\n",
        new SpreadsheetOutput(ImmutableList.of(
            Player.builder().name("Alpha, Alice").oldPoints(0).newPoints(500).build(),
            Player.builder().name("Beta, Bob").oldPoints(0).newPoints(499).build()
        )).getOutput());
  }
}
