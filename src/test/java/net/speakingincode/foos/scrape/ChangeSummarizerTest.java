package net.speakingincode.foos.scrape;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class ChangeSummarizerTest {
  private PointsBookPlayer player(String name, int points) {
    PointsBookPlayer p = new PointsBookPlayer();
    p.setName(name);
    p.setPoints(points);
    p.setLocal(1);
    return p;
  }
  
  @Test
  public void testEmptyInput() {
    ChangeSummarizer summarizer = new ChangeSummarizer(
        ImmutableList.<PointsBookPlayer>of(),
        ImmutableList.<PointsBookPlayer>of());
    assertEquals("", summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testNoChange() {
    ChangeSummarizer summarizer = new ChangeSummarizer(
        ImmutableList.of(player("Alice", 600), player("Bob", 600)),
        ImmutableList.of(player("Alice", 600), player("Bob", 600)));
    assertEquals("", summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testPointsIncrease() {
    ChangeSummarizer summarizer = new ChangeSummarizer(
        ImmutableList.of(player("Alice", 600)),
        ImmutableList.of(player("Alice", 650)));
    assertEquals("1st: Alice: 650 (+50)\n", summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testPointsDecrease() {
    ChangeSummarizer summarizer = new ChangeSummarizer(
        ImmutableList.of(player("Alice", 600)),
        ImmutableList.of(player("Alice", 550)));
    assertEquals("1st: Alice: 550 (-50)\n", summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testSecondPlace() {
    ChangeSummarizer summarizer = new ChangeSummarizer(
        ImmutableList.of(player("Alice", 600), player("Bob", 500)),
        ImmutableList.of(player("Alice", 550), player("Bob", 525)));
    assertEquals(
        "1st: Alice: 550 (-50)\n" +
        "2nd: Bob: 525 (+25)\n",
        summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testFirstPlaceTie() {
    ChangeSummarizer summarizer = new ChangeSummarizer(
        ImmutableList.of(player("Alice", 600), player("Bob", 500)),
        ImmutableList.of(player("Alice", 550), player("Bob", 550)));
    assertEquals(
        "1st: Alice: 550 (-50)\n" +
        "1st: Bob: 550 (+50, old rank: 2nd)\n",
        summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testFirstPlaceTieWithThird() {
    ChangeSummarizer summarizer = new ChangeSummarizer(
        ImmutableList.of(
            player("Alice", 600), player("Bob", 500), player("Claire", 400)),
        ImmutableList.of(
            player("Alice", 550), player("Bob", 550), player("Claire", 540)));
    assertEquals(
        "1st: Alice: 550 (-50)\n" +
        "1st: Bob: 550 (+50, old rank: 2nd)\n" +
        "3rd: Claire: 540 (+140)\n",
        summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testThreeWayTie() {
    ChangeSummarizer summarizer = new ChangeSummarizer(
        ImmutableList.of(
            player("Alice", 600), player("Bob", 500), player("Claire", 400)),
        ImmutableList.of(
            player("Alice", 550), player("Bob", 550), player("Claire", 550)));
    assertEquals(
        "1st: Alice: 550 (-50)\n" +
        "1st: Bob: 550 (+50, old rank: 2nd)\n" +
        "1st: Claire: 550 (+150, old rank: 3rd)\n",
        summarizer.getTopPlayerSummary());
  }
  
  @Test
  public void testRankChange() {
    ChangeSummarizer summarizer = new ChangeSummarizer(
        ImmutableList.of(
            player("Alice", 600), player("Bob", 500)),
        ImmutableList.of(
            player("Alice", 500), player("Bob", 525)));
    assertEquals(
        "1st: Bob: 525 (+25, old rank: 2nd)\n" +
        "2nd: Alice: 500 (-100, old rank: 1st)\n",
        summarizer.getTopPlayerSummary());
  }
}
