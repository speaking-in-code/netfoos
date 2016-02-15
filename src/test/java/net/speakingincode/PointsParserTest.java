package net.speakingincode;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Test;

import com.google.common.base.Charsets;

public class PointsParserTest {
  private PointsParser parser = new PointsParser();
  
  @Test
  public void parsesWellFormatted() throws Exception {
    Player player = parser.parse("Spredeman, Tony 7846 7846 7846 0 7878/7846");
    assertEquals("Spredeman, Tony", player.name());
    assertEquals("7846", player.currentPoints());
    assertEquals("7846", player.newPoints());
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
    assertEquals(null, player.currentPoints());
    assertEquals("3777", player.newPoints());
  }
  
  @Test
  public void parsesNameWithSpaces() throws Exception {
    Player p = parser.parse("Van Buskirk, Bruce No Data No Data 953 953 953/903");
    assertEquals("Van Buskirk, Bruce", p.name());
    assertEquals(null, p.currentPoints());
    assertEquals("953", p.newPoints());
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
