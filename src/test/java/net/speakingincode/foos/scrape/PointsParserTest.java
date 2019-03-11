package net.speakingincode.foos.scrape;

import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.Assert.assertEquals;

public class PointsParserTest {
  private PointsParser parser;

  @Before
  public void before() throws Exception {
    parser = new PointsParser(PointsBookFixture.forPlayers(
        PointsBookPlayer.builder()
            .setName("Spredeman, Tony")
            .setIfpActive(1)
            .build(),
        PointsBookPlayer.builder()
            .setName("Retired, Player")
            .setIfpActive(0)
            .build()));
  }

  @Test
  public void parsesWellFormatted() throws Exception {
    Player player = parser.parse("Spredeman, Tony 7846 7846 7846 0 7878/7846");
    assertEquals("Spredeman, Tony", player.name());
    assertEquals(7846, player.oldPoints());
    assertEquals(7846, player.newPoints());
  }

  @Test
  public void largeLocalPreferred() throws Exception {
    Player player = parser.parse("Spredeman, Tony 7846 7846 7850 4 7878/7846");
    assertEquals("Spredeman, Tony", player.name());
    assertEquals(7846, player.oldPoints());
    assertEquals(7850, player.newPoints());
  }

  @Test
  public void largeIfpPreferred() throws Exception {
    Player player = parser.parse("Spredeman, Tony 7900 7846 7850 4 7878/7846");
    assertEquals("Spredeman, Tony", player.name());
    assertEquals(7846, player.oldPoints());
    assertEquals(7900, player.newPoints());
  }

  @Test
  public void inactiveIfpIgnored() throws Exception {
    Player player = parser.parse("Retired, Player 7900 7846 7850 4 7878/7846");
    assertEquals("Retired, Player", player.name());
    assertEquals(7846, player.oldPoints());
    assertEquals(7850, player.newPoints());
  }

  @Test
  public void parsesHeader() throws Exception {
    Player player = parser.parse("NAME              BASE CURRENT UPDATED CHANGE HI/LOW");
    assertEquals(null, player);
  }

  @Test
  public void parsesPartialData() throws Exception {
    Player player = parser.parse("Pipkin, Jeff      3809 No Data 3777    3777   3809/3777");
    assertEquals("Pipkin, Jeff", player.name());
    assertEquals(0, player.oldPoints());
    assertEquals(3777, player.newPoints());
  }

  @Test
  public void parsesNameWithSpaces() throws Exception {
    Player p = parser.parse("Van Buskirk, Bruce No Data No Data 953 953 953/903");
    assertEquals("Van Buskirk, Bruce", p.name());
    assertEquals(0, p.oldPoints());
    assertEquals(953, p.newPoints());
  }

  @Test
  public void parsesGarbageLine() throws Exception {
    assertEquals(null, parser.parse("   285 285 299/27    "));
  }

  @Test
  public void parsesAllInput() throws Exception {
    try (InputStream testInput = getClass().getResourceAsStream("/points.txt");
         Reader asChars = new InputStreamReader(testInput, Charsets.UTF_8);
         BufferedReader reader = new BufferedReader(asChars)) {
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        parser.parse(line);
      }
    }
  }
}
