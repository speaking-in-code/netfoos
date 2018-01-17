package net.speakingincode.foos.scrape;

import java.io.IOException;
import java.time.LocalDate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class TournamentEditorTest {
  private Credentials credentials;
  private HtmlUnitDriver driver;
  
  @Before
  public void before() throws IOException {
    credentials = Credentials.load();
    driver = new HtmlUnitDriver();
    new NetfoosLogin(credentials, driver).login();
  }
  
  @After
  public void after() {
    driver.close();
  }
  
  @Test
  public void createsAndDeletes() throws IOException {
    TournamentEditor editor = new TournamentEditor(credentials, driver);
    String id = editor.create(makeTournament());
    editor.deleteTournament(id);
  }
  
  private Tournament makeTournament() {
    return Tournament.builder()
        .setName("Test Tournament")
        .setCity("Smallville")
        .setState("KS")
        .setDescription("Fun Tournament")
        .setLocation("Foos Club")
        .setAddress("101 Foosball St")
        .setZip("10001")
        .setDate(LocalDate.now())
        .build();
  }
  
  @Test(expected = IOException.class)
  public void deleteNotFound() throws IOException {
    TournamentEditor editor = new TournamentEditor(credentials, driver);
    editor.deleteTournament("51377");
  }
  
  @Test
  public void createEvent() throws Exception {
    TournamentEditor editor = new TournamentEditor(credentials, driver);
    String tournament = null;
    try {
      tournament = editor.create(makeTournament().toBuilder().setName("Create Event Test").build());
    } catch (IOException e) {
      DamnItLogger.log(driver);
      throw e;
    }
    try {
      editor.createEvent(SingleMatchEvent.builder()
          .tournamentId(tournament)
          .winnerPlayerOne("Alder, Wes")
          .winnerPlayerTwo("Adams, Clay")
          .loserPlayerOne("X, Jay")
          .loserPlayerTwo("X, Reg")
          .build());
    } finally {
      editor.deleteTournament(tournament);
    }
  }
  
  @Test(expected = IOException.class)
  public void createEventMissingPlayer() throws Exception {
    TournamentEditor editor = new TournamentEditor(credentials, driver);
    String tournament = editor.create(
        makeTournament().toBuilder().setName("No Such Player Test").build());
    try {
      editor.createEvent(SingleMatchEvent.builder()
          .tournamentId(tournament)
          .winnerPlayerOne("Nobody, Nohow")
          .winnerPlayerTwo("Adams, Clay")
          .loserPlayerOne("X, Jay")
          .loserPlayerTwo("X, Reg")
          .build());
    } finally {
      editor.deleteTournament(tournament);
    }
  }
}
