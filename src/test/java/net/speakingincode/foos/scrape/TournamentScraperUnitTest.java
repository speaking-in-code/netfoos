package net.speakingincode.foos.scrape;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import net.speakingincode.foos.scrape.TournamentResults.EventResults;
import net.speakingincode.foos.scrape.TournamentResults.Finish;

public class TournamentScraperUnitTest {
  private static final String url = "http://www.netfoos.com/manager/?nfs=509f3e33d2d59226d0cab00"
      + "92a47aebe&ncv=dk17rcgm32n8t&account=119&action=ITSF_Results_Export_1_0&nfts_mod_1=45760"
      + "&nfts_mod_2=45769";
  
  @Test
  public void parseSingles() throws IOException {
    EventResults event = TournamentScraper.parseResults(url, 
        "Saturday Singles and SnB\n" + 
        "2016-02-27\n" + 
        "Singles\n" + 
        "\n" + 
        "Finish\tLast Name (1)\tFirst Name (1)\tCountry (1)\tLast Name (2)\tFirst Name (2)\tCountry (2)\n" + 
        "1\tUddin\tMohammed\t\n" + 
        "2\tAragones\tSergie\t\n" + 
        "3\tCorioso\tSteve\t\n" + 
        "4\tCota\tRay\t\n" + 
        "5\tPitts\tJason\t\n" + 
        "5\tEaton\tBrian\t\n" + 
        "7\tAsis\tBernie\t\n" + 
        "7\tRado\tRod\t\n" + 
        "9\tHariharan\tBharath\t\n" + 
        "9\tHey\tJohn\t\n" +
        "");
    assertEquals("Saturday Singles and SnB", event.tournamentName());
    assertEquals("2016-02-27", event.date());
    assertEquals("Singles", event.eventName());
    assertEquals(Finish.builder().finish(1).playerOne("Mohammed U").build(),
        event.finishes().get(0));
    assertEquals(Finish.builder().finish(2).playerOne("Sergie A").build(),
        event.finishes().get(1));
    assertEquals(Finish.builder().finish(9).playerOne("Bharath H").build(),
        event.finishes().get(8));
    assertEquals(Finish.builder().finish(9).playerOne("John H").build(),
        event.finishes().get(9));
  }
  
  @Test
  public void parseDoubles() throws Exception {
    EventResults event = TournamentScraper.parseResults(url, 
        "Saturday Singles and SnB\n" + 
        "2016-02-27\n" + 
        "S ''n B\n" + 
        "\n" + 
        "Finish\tLast Name (1)\tFirst Name (1)\tCountry (1)\tLast Name (2)\tFirst Name (2)\tCountry (2)\n" + 
        "1\tAragones\tSergie\t\tLavigna\tTony\t\n" + 
        "2\tCorioso\tSteve\t\tSchlaefer\tPhil\t\n" + 
        "3\tCota\tRay\t\tEaton\tBrian\t\n" + 
        "4\tUddin\tMohammed\t\tCastillo\tJames\t\n" + 
        "5\tAsis\tBernie\t\tFurci\tNick\t\n" + 
        "5\tHariharan\tBharath\t\tTrok\tAndrey\t\n" + 
        "7\tPitts\tJason\t\tSana\tAlbert\t\n" + 
        "7\tReshetov\tDmitry\t\tRado\tRod\t\n");
    assertEquals("Saturday Singles and SnB", event.tournamentName());
    assertEquals("2016-02-27", event.date());
    assertEquals("S ''n B", event.eventName());
    assertEquals(Finish.builder()
        .finish(1)
        .playerOne("Sergie A")
        .playerTwo("Tony L").build(),
        event.finishes().get(0));
    assertEquals(Finish.builder()
        .finish(2)
        .playerOne("Steve C")
        .playerTwo("Phil S").build(),
        event.finishes().get(1));
  }
}
