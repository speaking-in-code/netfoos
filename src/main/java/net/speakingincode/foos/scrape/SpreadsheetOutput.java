package net.speakingincode.foos.scrape;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class SpreadsheetOutput {
  private static final Logger log = Logger.getLogger(SpreadsheetOutput.class.getName());
  
  private static final String SHEETS_UPDATE_SCRIPT_URL =
  "https://script.google.com/macros/s/AKfycbzENm8io5zdMM8WoB1uG0eR8tW9gGtmcLGTaZvsS1gbPvuaWoI/exec";
  
  private static final String SPREADSHEET_URL =
      "https://docs.google.com/spreadsheets/d/1LUAH8ZzsfN7VQwS27pLzS-0ksiAoxfgWXTlRS30j9pQ/pubhtml";
  
  private static final Gson gson = new Gson();
  private static final Splitter nameSplitter = Splitter.on(',').trimResults();
  private final ImmutableList<Player> byPoints;
  
  public SpreadsheetOutput(List<Player> player) {
    byPoints = Sorting.copySortedByNewPoints(player);
  }
  
  public String getDestinationUrl() {
    return SPREADSHEET_URL;
  }
  
  public void publishToGoogleSheets() throws IOException {
    // Each row in the spreadsheet has rank, name, and points.
    List<List<String>> spreadsheetData = Lists.newArrayList();
    int rank = 1;
    for (Player player : byPoints) {
      spreadsheetData.add(ImmutableList.of("" + rank, player.name(), "" + player.newPoints()));
      ++rank;
    }
    JsonObject req = new JsonObject();
    req.add("data", gson.toJsonTree(spreadsheetData));
    HttpResponse result = Request.Post(SHEETS_UPDATE_SCRIPT_URL)
        .bodyString(gson.toJson(req), ContentType.APPLICATION_JSON)
        .execute()
        .returnResponse();
    // For a successful result, app script returns a 302 to the actual content. Fetch that.
    Header location = result.getFirstHeader("Location");
    if (location != null) {
      Content actualContent = Request.Get(location.getValue()).execute().returnContent();
      log.info("Result: " + actualContent.asString());
    } else {
      // Failures are returned inline.
      log.info("Result: " + EntityUtils.toString(result.getEntity()));
    }
  }
  
  public String getOutput() {
    StringBuilder out = new StringBuilder();
    out.append("Last\tFirst\tPoints\n");
    for (Player player : byPoints) {
      List<String> name = nameSplitter.splitToList(player.name());
      out.append(name.get(0));
      out.append('\t');
      out.append(name.get(1));
      out.append('\t');
      out.append(player.newPoints());
      out.append('\n');
    }
    return out.toString();
  }
}
