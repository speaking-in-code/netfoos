package net.speakingincode.foos.scrape;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

/**
 * Handles reads and writes of the points book spreadsheet.
 */
public class PointsBook {  
  private static final String SHEETS_UPDATE_SCRIPT_URL =
  "https://script.google.com/macros/s/AKfycbzENm8io5zdMM8WoB1uG0eR8tW9gGtmcLGTaZvsS1gbPvuaWoI/exec";
  
  private static final String SPREADSHEET_URL =
      "https://docs.google.com/spreadsheets/d/1LUAH8ZzsfN7VQwS27pLzS-0ksiAoxfgWXTlRS30j9pQ/pubhtml";
  
  private static final Gson gson = new Gson();
  private final ImmutableMap<String, PointsBookPlayer> nameToPlayer;
  
  private PointsBook(PointsBookData pointsBook) {
    ImmutableMap.Builder<String, PointsBookPlayer> nameToPlayer = ImmutableMap.builder();
    for (PointsBookPlayer player : pointsBook.getPlayers()) {
      nameToPlayer.put(player.getName(), player);
    }
    this.nameToPlayer = nameToPlayer.build();
  }
  
  /**
   * Load the points book data from the spreadsheet.
   */
  public static PointsBook load() throws IOException {
    String text = readScriptResponse(Request.Get(SHEETS_UPDATE_SCRIPT_URL));
    return loadFromString(text);
  }
  
  /**
   * Load the points book data from a text string.
   */
  @VisibleForTesting
  public static PointsBook loadFromString(String text) throws IOException {
    return new PointsBook(gson.fromJson(text, PointsBookData.class));
  }
  
  /**
   * @return the raw data in the spreadsheet.
   */
  @VisibleForTesting
  ImmutableMap<String, PointsBookPlayer> getPointsBook() {
    return nameToPlayer;
  }
  
  private static String readScriptResponse(Request req) throws IOException {
    HttpResponse result = req.execute().returnResponse();
    // For a successful result, app script returns a 302 to the actual content. Fetch that.
    Header location = result.getFirstHeader("Location");
    if (location != null) {
      Content actualContent = Request.Get(location.getValue()).execute().returnContent();
      return actualContent.asString();
    }
    // Failures are returned inline.
    return EntityUtils.toString(result.getEntity());
  }
  
  /**
   * Update the data in the spreadsheet; only the players in the changed list are updated.
   * 
   * Note that this also marks all of the changed players as locals.
   * 
   * @return a new points book, with the updates.
   */
  public PointsBook updateAllPlayers(ImmutableList<Player> all) throws IOException {
    PointsBookData newData = getUpdate(all);
    String text = readScriptResponse(Request.Post(SHEETS_UPDATE_SCRIPT_URL)
        .bodyString(gson.toJson(newData), ContentType.APPLICATION_JSON));
    if (!text.equals("Completed response")) {
      throw new IOException(text);
    }
    return PointsBook.load();
  }
  
  /**
   * Get the set of all of the players in the database.
   */
  public ImmutableCollection<PointsBookPlayer> getPlayers() {
    return nameToPlayer.values();
  }
  
  @VisibleForTesting
  PointsBookData getUpdate(ImmutableList<Player> all) {    
    List<PointsBookPlayer> newData = Lists.newArrayList();
    for (Player player : all) {
      PointsBookPlayer bookPlayer = new PointsBookPlayer();
      bookPlayer.setName(player.name());
      bookPlayer.setPoints(player.newPoints());
      PointsBookPlayer oldData = nameToPlayer.get(player.name());
      if (oldData == null || oldData.getPoints() != player.newPoints()) {
        bookPlayer.setLocal(1);
      } else {
        bookPlayer.setLocal(oldData.getLocal());
      }
      newData.add(bookPlayer);
    }
    Collections.sort(newData, compareByPoints);
    PointsBookData data = new PointsBookData();
    data.setPlayers(newData);
    return data;
  }
  
  private static Comparator<PointsBookPlayer> compareByPoints = new Comparator<PointsBookPlayer>() {
    @Override
    public int compare(PointsBookPlayer o1, PointsBookPlayer o2) {
      return o2.getPoints() - o1.getPoints();
    }
  };
  
  public String getDestinationUrl() {
    return SPREADSHEET_URL;
  }
}
