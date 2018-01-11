package net.speakingincode.foos.scrape;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.common.collect.ImmutableSet;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.*;

public class PlayerListCheckerTest {
  private HtmlUnitDriver driver;
  private PlayerListChecker checker;
  
  @Before
  public void before() throws IOException {
    Credentials credentials = Credentials.load();
    driver = new HtmlUnitDriver();
    checker = new PlayerListChecker(credentials, driver);
  }
  
  @After
  public void after() throws IOException {
    driver.close();
  }
  
  @Test
  public void allFound() throws IOException {
    ImmutableSet<String> missing = checker.findMissingPlayers(ImmutableSet.of(
        "Eaton, Brian", "Fick, Jim"));
    assertThat(missing, empty());
  }
  
  @Test
  public void nickNamesIgnored() throws IOException {
    ImmutableSet<String> missing = checker.findMissingPlayers(ImmutableSet.of(
        "Rado, Rod"));
    assertThat(missing, empty());
  }
  
  @Test
  public void oneMissing() throws IOException {
    ImmutableSet<String> missing = checker.findMissingPlayers(ImmutableSet.of(
        "Eaton, Brian", "Owens, Nobody"));
    assertThat(missing, contains("Owens, Nobody"));
  }
  
  @Test
  public void allMissing() throws IOException {
    ImmutableSet<String> missing = checker.findMissingPlayers(ImmutableSet.of(
        "Match, None", "Owens, Nobody"));
    assertThat(missing, contains("Match, None", "Owens, Nobody"));
  }
}
