package net.speakingincode.foos.scrape;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import net.speakingincode.foos.scrape.ChangeSummarizer;
import net.speakingincode.foos.scrape.Player;

public class ChangeSummarizerTest {
  
  private Player.Builder emptyBuilder() {
    return Player.builder().newBasePoints(0).oldBasePoints(0);
  }
  
  @Test
  public void testEmptyInput() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.<Player>of());
    assertEquals("", summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testNoChange() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        emptyBuilder().name("Alice").oldPoints(600).newPoints(600).build(),
        emptyBuilder().name("Bob").oldPoints(600).newPoints(600).build()
        ));
    assertEquals("", summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testPointsIncrease() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        emptyBuilder().name("Alice").oldPoints(600).newPoints(650).build()
        ));
    assertEquals("1st: Alice: 650 (+50)\n", summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testPointsDecrease() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        emptyBuilder().name("Alice").oldPoints(600).newPoints(550).build()
        ));
    assertEquals("1st: Alice: 550 (-50)\n", summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testSecondPlace() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        emptyBuilder().name("Alice").oldPoints(600).newPoints(550).build(),
        emptyBuilder().name("Bob").oldPoints(500).newPoints(525).build()
        ));
    assertEquals(
        "1st: Alice: 550 (-50)\n" +
        "2nd: Bob: 525 (+25)\n",
        summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testFirstPlaceTie() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        emptyBuilder().name("Alice").oldPoints(600).newPoints(550).build(),
        emptyBuilder().name("Bob").oldPoints(500).newPoints(550).build()
        ));
    assertEquals(
        "1st: Alice: 550 (-50)\n" +
        "1st: Bob: 550 (+50, old rank: 2nd)\n",
        summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testFirstPlaceTieWithThird() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        emptyBuilder().name("Alice").oldPoints(600).newPoints(550).build(),
        emptyBuilder().name("Bob").oldPoints(500).newPoints(550).build(),
        emptyBuilder().name("Claire").oldPoints(400).newPoints(540).build()

        ));
    assertEquals(
        "1st: Alice: 550 (-50)\n" +
        "1st: Bob: 550 (+50, old rank: 2nd)\n" +
        "3rd: Claire: 540 (+140)\n",
        summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testThreeWayTie() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        emptyBuilder().name("Alice").oldPoints(600).newPoints(550).build(),
        emptyBuilder().name("Bob").oldPoints(500).newPoints(550).build(),
        emptyBuilder().name("Claire").oldPoints(400).newPoints(550).build()
        ));
    assertEquals(
        "1st: Alice: 550 (-50)\n" +
        "1st: Bob: 550 (+50, old rank: 2nd)\n" +
        "1st: Claire: 550 (+150, old rank: 3rd)\n",
        summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testRankChange() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        emptyBuilder().name("Alice").oldPoints(600).newPoints(500).build(),
        emptyBuilder().name("Bob").oldPoints(500).newPoints(525).build()
        ));
    assertEquals(
        "1st: Bob: 525 (+25, old rank: 2nd)\n" +
        "2nd: Alice: 500 (-100, old rank: 1st)\n",
        summarizer.getTopPlayerSummary());
  }
}
