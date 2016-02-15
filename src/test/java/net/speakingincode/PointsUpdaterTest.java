package net.speakingincode;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

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
        .currentPoints(null)
        .newPoints("450")
        .build();
    updater.beginUpdate();
    updater.updatePoints(p);
  }
  
  @Test
  public void updateTwoPlayers() throws Exception {
    Player p1 = Player.builder()
        .name("?, Steve")
        .currentPoints(null)
        .newPoints("499")
        .build();
    Player p2 = Player.builder()
        .name(", Sterling")
        .currentPoints(null)
        .newPoints("501")
        .build();
    updater.beginUpdate();
    updater.updatePoints(p1);
    updater.updatePoints(p2);
  }
}
