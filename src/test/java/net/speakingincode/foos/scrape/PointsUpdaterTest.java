package net.speakingincode.foos.scrape;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.NetfoosLogin;
import net.speakingincode.foos.scrape.Player;
import net.speakingincode.foos.scrape.PointsUpdater;

public class PointsUpdaterTest {
  private static WebDriver driver;
  private static PointsUpdater updater;

  @BeforeClass
  public static void before() throws Exception {
    driver = new HtmlUnitDriver();
    Credentials credentials = Credentials.load();
    NetfoosLogin login = new NetfoosLogin(credentials, driver);
    login.login();
    updater = new PointsUpdater(driver);
  }
  
  @AfterClass
  public static void after() {
    driver.close();
  }
  
  @Test
  public void updateOddMan() throws Exception {
    Player p = Player.builder()
        .name("?, Steve")
        .oldPoints(0)
        .newPoints(450)
        .build();
    updater.beginUpdate();
    updater.updatePoints(p);
  }
  
  @Test
  public void updateTwoPlayers() throws Exception {
    Player p1 = Player.builder()
        .name("?, Steve")
        .oldPoints(0)
        .newPoints(499)
        .build();
    Player p2 = Player.builder()
        .name(", Sterling")
        .oldPoints(0)
        .newPoints(501)
        .build();
    updater.beginUpdate();
    updater.updatePoints(p1);
    updater.updatePoints(p2);
  }
}
