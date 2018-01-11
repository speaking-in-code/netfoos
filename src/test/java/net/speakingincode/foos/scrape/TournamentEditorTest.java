package net.speakingincode.foos.scrape;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

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
    Tournament t = Tournament.builder()
        .setName("Test Tournament")
        .setCity("Smallville")
        .setState("KS")
        .setDescription("Fun Tournament")
        .setLocation("Foos Club")
        .setDate(LocalDate.of(2018, 01, 03))
        .build();
    TournamentEditor editor = new TournamentEditor(driver, credentials);
    String id = editor.create(t);
    editor.deleteTournament(id);
  }
  
  @Test(expected = IOException.class)
  public void deleteNotFound() throws IOException {
    TournamentEditor editor = new TournamentEditor(driver, credentials);
    editor.deleteTournament("51377");
  }
}
