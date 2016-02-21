package net.speakingincode;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class ChangeSummarizerTest {
  @Test
  public void testEmptyInput() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.<Player>of());
    assertEquals("", summarizer.getSummary());
  }
  
  @Test
  public void testNoChange() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        Player.builder().name("Alice").oldPoints(600).newPoints(600).build(),
        Player.builder().name("Bob").oldPoints(600).newPoints(600).build()
        ));
    assertEquals("", summarizer.getSummary());
  }
  
  @Test
  public void testPointsIncrease() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        Player.builder().name("Alice").oldPoints(600).newPoints(650).build()
        ));
    assertEquals("Alice: 650 (+50). Rank: 1st.\n", summarizer.getSummary());
  }
  
  @Test
  public void testPointsDecrease() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        Player.builder().name("Alice").oldPoints(600).newPoints(550).build()
        ));
    assertEquals("Alice: 550 (-50). Rank: 1st.\n", summarizer.getSummary());
  }
  
  @Test
  public void testSecondPlace() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        Player.builder().name("Alice").oldPoints(600).newPoints(550).build(),
        Player.builder().name("Bob").oldPoints(500).newPoints(525).build()
        ));
    assertEquals(
        "Alice: 550 (-50). Rank: 1st.\n" +
        "Bob: 525 (+25). Rank: 2nd.\n",
        summarizer.getSummary());
  }
  
  @Test
  public void testFirstPlaceTie() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        Player.builder().name("Alice").oldPoints(600).newPoints(550).build(),
        Player.builder().name("Bob").oldPoints(500).newPoints(550).build()
        ));
    assertEquals(
        "Alice: 550 (-50). Rank: 1st.\n" +
        "Bob: 550 (+50). New rank: 1st, was 2nd.\n",
        summarizer.getSummary());
  }
  
  @Test
  public void testFirstPlaceTieWithThird() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        Player.builder().name("Alice").oldPoints(600).newPoints(550).build(),
        Player.builder().name("Bob").oldPoints(500).newPoints(550).build(),
        Player.builder().name("Claire").oldPoints(400).newPoints(540).build()

        ));
    assertEquals(
        "Alice: 550 (-50). Rank: 1st.\n" +
        "Bob: 550 (+50). New rank: 1st, was 2nd.\n" +
        "Claire: 540 (+140). Rank: 3rd.\n",
        summarizer.getSummary());
  }
  
  @Test
  public void testThreeWayTie() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        Player.builder().name("Alice").oldPoints(600).newPoints(550).build(),
        Player.builder().name("Bob").oldPoints(500).newPoints(550).build(),
        Player.builder().name("Claire").oldPoints(400).newPoints(550).build()

        ));
    assertEquals(
        "Alice: 550 (-50). Rank: 1st.\n" +
        "Bob: 550 (+50). New rank: 1st, was 2nd.\n" +
        "Claire: 550 (+150). New rank: 1st, was 3rd.\n",
        summarizer.getSummary());
  }
  
  @Test
  public void testRankChange() {
    ChangeSummarizer summarizer = new ChangeSummarizer(ImmutableList.of(
        Player.builder().name("Alice").oldPoints(600).newPoints(500).build(),
        Player.builder().name("Bob").oldPoints(500).newPoints(525).build()
        ));
    assertEquals(
        "Bob: 525 (+25). New rank: 1st, was 2nd.\n" +
        "Alice: 500 (-100). New rank: 2nd, was 1st.\n",
        summarizer.getSummary());
  }
  
  @Test
  public void testStringRanks() {
    assertEquals("1st", ChangeSummarizer.toStringRank(0));
    assertEquals("2nd", ChangeSummarizer.toStringRank(1));
    assertEquals("3rd", ChangeSummarizer.toStringRank(2));
    assertEquals("4th", ChangeSummarizer.toStringRank(3));
    assertEquals("5th", ChangeSummarizer.toStringRank(4));
    assertEquals("6th", ChangeSummarizer.toStringRank(5));
    assertEquals("7th", ChangeSummarizer.toStringRank(6));
    assertEquals("8th", ChangeSummarizer.toStringRank(7));
    assertEquals("9th", ChangeSummarizer.toStringRank(8));
    assertEquals("10th", ChangeSummarizer.toStringRank(9));
    assertEquals("11th", ChangeSummarizer.toStringRank(10));
    assertEquals("12th", ChangeSummarizer.toStringRank(11));
    assertEquals("13th", ChangeSummarizer.toStringRank(12));
    assertEquals("14th", ChangeSummarizer.toStringRank(13));
    assertEquals("15th", ChangeSummarizer.toStringRank(14));
    assertEquals("16th", ChangeSummarizer.toStringRank(15));
    assertEquals("17th", ChangeSummarizer.toStringRank(16));
    assertEquals("18th", ChangeSummarizer.toStringRank(17));
    assertEquals("19th", ChangeSummarizer.toStringRank(18));
    assertEquals("20th", ChangeSummarizer.toStringRank(19));
    assertEquals("21st", ChangeSummarizer.toStringRank(20));
    assertEquals("22nd", ChangeSummarizer.toStringRank(21));
    assertEquals("23rd", ChangeSummarizer.toStringRank(22));
    assertEquals("24th", ChangeSummarizer.toStringRank(23));
    assertEquals("25th", ChangeSummarizer.toStringRank(24));
  }
}
