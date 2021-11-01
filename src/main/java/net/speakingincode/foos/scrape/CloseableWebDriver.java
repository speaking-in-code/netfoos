package net.speakingincode.foos.scrape;

import org.openqa.selenium.WebDriver;

public class CloseableWebDriver implements AutoCloseable {
  private final WebDriver driver;

  public CloseableWebDriver(WebDriver driver) {
    this.driver = driver;
  }

  public WebDriver getDriver() {
    return driver;
  }

  @Override
  public void close() {
    if (driver != null) {
      driver.close();
    }
  }
}
