package net.speakingincode.foos.scrape;

import com.google.common.io.Files;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Logs HTML page to /tmp/dammit.html when something goes wrong.
 */
public class DamnItLogger {
  private static final Logger log = Logger.getLogger(DamnItLogger.class.getName());
  private static File dammit = new File("/tmp/dammit.html");

  public static void log(WebDriver driver) {
    log.warning("Writing error " + dammit.getPath());
    try {
      Files.asCharSink(dammit, com.google.common.base.Charsets.UTF_8).write(driver.getPageSource());
    } catch (IOException e) {
      log.warning("Failed to write error to " + dammit.getPath() + ": " + e);
    }
  }
}
