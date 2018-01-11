package net.speakingincode.foos.scrape;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class TournamentEditor {
  private final WebDriver driver;
  private final Credentials credentials;
  
  public TournamentEditor(WebDriver driver, Credentials credentials) {
    this.driver = driver;
    this.credentials = credentials;
  }
  
  /**
   * Create a tournament.
   * 
   * @return netfoos tournament ID.
   */
  public String create(Tournament tournament) throws IOException {
    driver.findElement(By.linkText("Add New Tournament")).click();
    checkPageContains("Add New Tournament");
    setText("eventname", tournament.name());
    selectField("netfoos_start_yyyy", tournament.date().getYear() + "");
    selectFieldValue("netfoos_start_mm", twoDigitPad(tournament.date().getMonthValue()));
    selectField("netfoos_start_dd", twoDigitPad(tournament.date().getDayOfMonth()));
    selectField("netfoos_end_yyyy", tournament.date().getYear() + "");
    selectFieldValue("netfoos_end_mm", twoDigitPad(tournament.date().getMonthValue()));
    selectField("netfoos_end_dd", twoDigitPad(tournament.date().getDayOfMonth()));
    setText("description", tournament.description());
    setText("locname", tournament.location());
    setText("loccity", tournament.city());
    setText("locstate", tournament.state());
    WebElement toSave = null;
    List<WebElement> submits = driver.findElements(By.name("Submit"));
    for (WebElement submit : submits) {
      if ("Save Tournament".equals(submit.getAttribute("value"))) {
        toSave = submit;
        break;
      }
    }
    if (toSave == null) {
      throw new IOException("Could not find Submit button.");
    }
    toSave.click();
    checkPageContains("Tournament Added Successfully");
    checkPageContains(tournament.name());
    TournamentScraper scraper = new TournamentScraper(driver, credentials);
    TournamentResults result = scraper.getOneResult(
        String.format("%s (%04d-%02d-%02d)", tournament.name(), tournament.date().getYear(),
            tournament.date().getMonthValue(), tournament.date().getDayOfMonth()));
    return result.tournamentId();
  }
  
  private void setText(String name, String value) {
    driver.findElement(By.name(name)).sendKeys(value);
  }
  
  private void selectField(String name, String value) {
    new Select(driver.findElement(By.name(name))).selectByVisibleText(value);
  }
  
  private void selectFieldValue(String name, String value) {
    new Select(driver.findElement(By.name(name))).selectByValue(value);
  }
  
  private String twoDigitPad(int number) {
    return String.format("%02d", number);
  }
  
  private void checkPageContains(String text) throws IOException {
    String page = driver.getPageSource();
    if (!page.contains(text)) {
      throw new IOException("Could not find " + text + " in page " + page);
    }
  }
  
  /**
   * Delete a tournament.
   * 
   * @param netfoosId the ID of the tournament.
   * @throws IOException 
   */
  public void deleteTournament(String netfoosId) throws IOException {
    driver.findElement(By.linkText("Manage Tournaments")).click();
    checkPageContains("Manage Tournaments");
    List<WebElement> deleteLinks = driver.findElements(By.linkText("Delete"));
    WebElement deleter = null;
    for (WebElement deleteLink : deleteLinks) {
      if (deleteLink.getAttribute("href").contains("&netfoos_id=" + netfoosId)) {
        deleter = deleteLink;
        break;
      }
    }
    if (deleter == null) {
      throw new IOException("Could not find tournament with ID " + netfoosId);
    }
    deleter.click();
    checkPageContains("ALL events within this tournament will be deleted.");
    driver.findElement(By.partialLinkText("Yes, Delete This Tournament")).click();
    checkPageContains("Successfully Deleted");
  }
}
