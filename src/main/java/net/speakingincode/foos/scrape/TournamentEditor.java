package net.speakingincode.foos.scrape;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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
    new NetfoosLogin(credentials, driver).login();
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
    driver.findElement(MoreBy.submitValue("Save Tournament")).click();
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
    new NetfoosLogin(credentials, driver).login();
    driver.findElement(By.linkText("Manage Tournaments")).click();
    checkPageContains("Manage Tournaments");
    WebElement deleter = filterForTournamentId(driver.findElements(By.linkText("Delete")),
        netfoosId);
    if (deleter == null) {
      throw new IOException("Could not find tournament with ID " + netfoosId);
    }
    deleter.click();
    checkPageContains("ALL events within this tournament will be deleted.");
    driver.findElement(By.partialLinkText("Yes, Delete This Tournament")).click();
    checkPageContains("Successfully Deleted");
  }
  
  /**
   * Adds an event record.
   * 
   * @return netfoos event ID
   */
  public String createEvent(SingleMatchEvent event) throws IOException {
    new NetfoosLogin(credentials, driver).login();
    driver.findElement(By.linkText("Manage Tournaments")).click();
    checkPageContains("Manage Tournaments");
    driver.findElement(MoreBy.linkTextAndArg("Add Event",
        "netfoos_sub_id=" + event.tournamentId())).click();
    checkPageContains("Add New Event");
    WebElement tournName = driver.findElement(By.name("tournname"));
    tournName.sendKeys("Monster DYP Seeding Round");
    Select chartUsed = new Select(driver.findElement(By.name("charttype")));
    chartUsed.selectByVisibleText("Single Elimination: Standard 8 Team Chart");
    driver.findElement(MoreBy.submitValue("Save Event")).click();
    checkPageContains("Event Added Successfully");
    
    // Search for the <td> element that has a green link in it. That's the edit event link.
    List<WebElement> tds = driver.findElements(By.tagName("td"));
    WebElement editResults = null;
    for (WebElement td : tds) {
      if ("#CCFFCC".equals(td.getAttribute("bgcolor"))) {
        editResults = td.findElement(By.linkText("Edit Results"));
        break;
      }
    }
    ParsedUrl url = ParsedUrl.parse(editResults.getAttribute("href"));
    String eventId = url.getQueryArgs().get("netfoos_id").iterator().next();
    editResults.click();
    checkPageContains("Manage Event Results");
    
    // Create the chart.
    driver.findElement(By.linkText("Create Chart")).click();
    checkPageContains("Create Initial Chart");
    selectPlayer("P1T1", event.winnerPlayerOne());
    selectPlayer("P2T1", event.winnerPlayerTwo());
    selectPlayer("P1T2", event.loserPlayerOne());
    selectPlayer("P2T2", event.loserPlayerTwo());
    driver.findElement(MoreBy.submitValue("Create Chart")).click();
    checkPageContains("Initial Bye Matches Auto Completed");
    
    // Enter the results
    driver.findElement(By.linkText("Enter Tournament Director Admin")).click();
    checkPageContains("Select Winner");
    // Autofill the bye matches by clicking this link.
    driver.findElement(By.linkText("Back to Manage Event")).click();
    // Now enter tournament admin, to finish the tournament.
    driver.findElement(By.linkText("Enter Tournament Director Admin")).click();
    Select results = new Select(driver.findElement(By.name("matchresults8")));
    results.selectByValue("T1");
    driver.findElement(MoreBy.submitValue("Save Winner")).click();
    checkPageContains("No Matches To Play");
    return eventId;
  }

  private void selectPlayer(String inputName, String playerName) throws IOException {
    Select select = new Select(driver.findElement(By.name(inputName)));
    try {
      select.selectByVisibleText(playerName);
    } catch (NoSuchElementException e) {
      throw new IOException("Player not found: " + playerName, e);
    }
  }

  private WebElement filterForTournamentId(Iterable<WebElement> links, String netfoosId) {
    for (WebElement link : links) {
      if (link.getAttribute("href").contains("&netfoos_id=" + netfoosId)) {
        return link;
      }
    }
    return null;
  }
}
