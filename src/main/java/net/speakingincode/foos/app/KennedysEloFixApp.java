package net.speakingincode.foos.app;

import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.common.collect.Lists;

import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.NetfoosLogin;

public class KennedysEloFixApp {
  private static final Logger logger = Logger.getLogger(KennedysEloFixApp.class.getName());

  public static void main(String[] args) throws Exception {
    WebDriver driver = new HtmlUnitDriver();
    Credentials credentials = Credentials.load();
    new NetfoosLogin(credentials, driver).login();
    driver.findElement(By.linkText("Manage Tournaments")).click();
    driver.findElement(By.linkText("Show All")).click();
    List<WebElement> trs = driver.findElements(By.tagName("tr"));
    List<String> linksToFix = Lists.newArrayList();
    for (WebElement el : trs) {
      String text = el.getText();
      if (!text.contains("DYP") || !text.contains("K=0")) {
        continue;
      }
      WebElement edit = el.findElement(By.linkText("DYP"));
      linksToFix.add(edit.getAttribute("href"));
    }
    logger.info("Fixing " + linksToFix.size() + " events.");
    for (String link : linksToFix) {
      fixEvent(driver, link);
      //System.out.println(link);
      //driver.get(link);
    }
  }
  
  private static void fixEvent(WebDriver driver, String editLink) {
    logger.info("Fixing : " + editLink);
    driver.get(editLink);
    WebElement elokValue = driver.findElement(By.id("elok"));
    elokValue.clear();
    elokValue.sendKeys("32");
    WebElement submit = driver.findElement(By.name("Submit"));
    submit.click();
  }
}
