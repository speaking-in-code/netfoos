package net.speakingincode;

import java.io.IOException;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class NetfoosLogin {
  private static final Logger logger = Logger.getLogger(NetfoosLogin.class.getName());
  private final Credentials credentials;
  private final WebDriver driver;
  
  public NetfoosLogin(Credentials credentials, WebDriver driver) {
    this.credentials = credentials;
    this.driver = driver;
  }

  public void login() throws IOException {
    driver.get("http://www.netfoos.com/manager");
    if (!driver.getTitle().equals("NetFoos.com Admin - Login")) {
      throw new IOException("Did not find login page: " + driver.getPageSource());
    }
    WebElement username = driver.findElement(By.id("username"));
    username.sendKeys(credentials.username());
    WebElement password = driver.findElement(By.id("password"));
    password.sendKeys(credentials.password());
    WebElement login = driver.findElement(By.name("Submit"));
    login.click();
    String page = driver.getPageSource();
    if (!page.contains("San Francisco Bay Area Foosball")) {
      throw new IOException("Failed to login: " + page);
    }
    logger.info("Logged in.");
    return;
  }
}
