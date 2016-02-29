package net.speakingincode.foos.scrape;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RankStringsTest {
  @Test
  public void testStringRanks() {
    assertEquals("1st", RankStrings.toStringRank(0));
    assertEquals("2nd", RankStrings.toStringRank(1));
    assertEquals("3rd", RankStrings.toStringRank(2));
    assertEquals("4th", RankStrings.toStringRank(3));
    assertEquals("5th", RankStrings.toStringRank(4));
    assertEquals("6th", RankStrings.toStringRank(5));
    assertEquals("7th", RankStrings.toStringRank(6));
    assertEquals("8th", RankStrings.toStringRank(7));
    assertEquals("9th", RankStrings.toStringRank(8));
    assertEquals("10th", RankStrings.toStringRank(9));
    assertEquals("11th", RankStrings.toStringRank(10));
    assertEquals("12th", RankStrings.toStringRank(11));
    assertEquals("13th", RankStrings.toStringRank(12));
    assertEquals("14th", RankStrings.toStringRank(13));
    assertEquals("15th", RankStrings.toStringRank(14));
    assertEquals("16th", RankStrings.toStringRank(15));
    assertEquals("17th", RankStrings.toStringRank(16));
    assertEquals("18th", RankStrings.toStringRank(17));
    assertEquals("19th", RankStrings.toStringRank(18));
    assertEquals("20th", RankStrings.toStringRank(19));
    assertEquals("21st", RankStrings.toStringRank(20));
    assertEquals("22nd", RankStrings.toStringRank(21));
    assertEquals("23rd", RankStrings.toStringRank(22));
    assertEquals("24th", RankStrings.toStringRank(23));
    assertEquals("25th", RankStrings.toStringRank(24));
  }
}
