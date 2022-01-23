package net.speakingincode.foos.app;

import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.univocity.parsers.tsv.TsvWriter;
import com.univocity.parsers.tsv.TsvWriterSettings;
import net.speakingincode.foos.scrape.Credentials;
import net.speakingincode.foos.scrape.GsonUtil;
import net.speakingincode.foos.scrape.NetfoosToFileCache;
import net.speakingincode.foos.scrape.NetfoosToFileMetadata;
import net.speakingincode.foos.scrape.TournamentResults;
import net.speakingincode.foos.scrape.TournamentScraper;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class NetfoosExportApp {
  private static final Logger logger = Logger.getLogger(EloPlayerReport.class.getName());
  private static final Path CACHE_PATH = Paths.get(".netfooscache");
  private static final Gson gson = GsonUtil.gson();

  public static void main(String[] args) throws IOException {
    NetfoosToFileCache cache = null;
    try {
      cache = loadCache();
      cache = updateCache(cache);
    } catch (IOException e) {
      cache = initCache();
    }
    copyEverything(cache);
    convertToTSV();
    System.exit(0);
  }

  private static NetfoosToFileCache loadCache() throws IOException {
    logger.info("Loading cache");
    try (BufferedReader reader = Files.newBufferedReader(
        CACHE_PATH, StandardCharsets.UTF_8)) {
      return gson.fromJson(reader, NetfoosToFileCache.class);
    }
  }

  private static NetfoosToFileCache initCache() throws IOException {
    logger.info("No cache available, creating from scratch");
    TournamentScraper scraper = new TournamentScraper(new HtmlUnitDriver(), Credentials.load());
    NetfoosToFileCache cache = NetfoosToFileCache.create(scraper.getEventList());
    writeCache(cache);
    return loadCache();
  }

  private static NetfoosToFileCache updateCache(NetfoosToFileCache oldCache) throws IOException {
    logger.info("Updating cache");
    Set<String> alreadyLoaded = Sets.newHashSet();
    for (NetfoosToFileMetadata event : oldCache.events()) {
      if (event.tournamentName() != null) {
        alreadyLoaded.add(event.tournamentName());
      }
    }
    ImmutableList.Builder<NetfoosToFileMetadata> refresh = ImmutableList.builder();
    refresh.addAll(oldCache.events());
    TournamentScraper scraper = new TournamentScraper(new HtmlUnitDriver(), Credentials.load());
    for (NetfoosToFileMetadata newEvent : scraper.getEventList()) {
      if (alreadyLoaded.contains(newEvent.tournamentName())) {
        continue;
      }
      logger.info("New event: " + newEvent.tournamentName());
      refresh.add(newEvent);
    }
    NetfoosToFileCache updated = NetfoosToFileCache.create(refresh.build());
    writeCache(updated);
    return loadCache();
  }

  private static void writeCache(NetfoosToFileCache cache) throws IOException {
    int count = 0;
    for (NetfoosToFileMetadata event : cache.events()) {
      if (event.results() != null) {
        count++;
      }
    }
    logger.info(String.format("Writing new cache file, %d/%d complete", count, cache.events().size()));
    try (BufferedWriter writer = Files.newBufferedWriter(CACHE_PATH, StandardCharsets.UTF_8)) {
      gson.toJson(cache, writer);
      writer.flush();
    }
  }

  private static void copyEverything(NetfoosToFileCache cache) throws IOException {
    TournamentScraper scraper = new TournamentScraper(new HtmlUnitDriver(), Credentials.load());
    List<NetfoosToFileMetadata> events = Lists.newArrayList(cache.events().iterator());
    for (int i = 0; i < events.size(); ++i) {
      NetfoosToFileMetadata event = events.get(i);
      if (event.results() == null) {
        logger.info(String.format("Loading %d/%d, %s", i + 1, events.size(), event.tournamentName()));
        TournamentResults results = scraper.getOneResult(event.tournamentName());
        event = event.toBuilder().results(results).build();
        events.set(i, event);
        cache = NetfoosToFileCache.create(events);
        writeCache(cache);
      }
    }
  }

  private static void convertToTSV() throws IOException {
    NetfoosToFileCache cache = loadCache();
    writeResults(cache.events().stream().map(NetfoosToFileMetadata::results));
  }

  private static final int MAX_EVENTS_PER_TOURNAMENT = 8;

  @AutoValue
  abstract static class UniqueRecord {
    public abstract String name();
    public abstract String players();
    public abstract String date();

    public static UniqueRecord create(String name, String players, String date) {
      return new AutoValue_NetfoosExportApp_UniqueRecord(name, players, date);
    }
  }

  private static void writeResults(Stream<TournamentResults> results) {
    File out = new File("netfoos.tsv");
    logger.info("Writing netfoos data to " + out.getPath());
    TsvWriter writer = new TsvWriter(out, StandardCharsets.UTF_8, new TsvWriterSettings());
    writer.writeHeaders("Name", "Location", "Players", "Date");
    ConcurrentHashMap.KeySetView<UniqueRecord, Boolean> written = ConcurrentHashMap.newKeySet();
    results.forEach((result) -> {
      if (result == null || result.events().size() == 0) {
        return;
      }
      if (result.tournamentName().contains("Monster")) {
        writeSwiss(written, "Monster DYP", writer, result);
      } else if (result.events().size() > MAX_EVENTS_PER_TOURNAMENT) {
        writeSwiss(written, "Swiss", writer, result);
      } else {
        writeTraditionalEvent(written, writer, result);
      }
    });
    writer.close();
    logger.info("Wrote " + written.size() + " events");
  }

  private static void writeTraditionalEvent(ConcurrentHashMap.KeySetView<UniqueRecord, Boolean> written,
      TsvWriter writer, TournamentResults result) {
    for (TournamentResults.EventResults event : result.events()) {
      String name = event.eventName();
      String date = event.date();
      String players = getPlayers(event);
      if (players.isEmpty()) {
        continue;
      }
      if (!written.add(UniqueRecord.create(name, players, date))) {
        return;
      }
      String location = guessLocation(result);
      writer.writeRow(name, location, players, date);
    }
  }

  private static void writeSwiss(ConcurrentHashMap.KeySetView<UniqueRecord, Boolean> written, String name,
      TsvWriter writer, TournamentResults result) {
    if (result.events().isEmpty()) {
      return;
    }
    String date = result.events().get(0).date();
    Set<String> playerSet = Sets.newHashSet();
    for (TournamentResults.EventResults event : result.events()) {
      for (TournamentResults.Finish finish : event.finishes()) {
        playerSet.add(finish.playerOne());
        if (finish.playerTwo() != null) {
          playerSet.add(finish.playerTwo());
        }
      }
    }
    String players = formatPlayers(playerSet);
    if (players.isEmpty()) {
      return;
    }
    if (!written.add(UniqueRecord.create(name, players, date))) {
      return;
    }
    String location = guessLocation(result);
    writer.writeRow(name, location, players, date);
  }

  private static final String KENNEDYS = "Kennedy's";
  private static final String CALIFORNIA_BILLIARDS = "California Billiards";

  private static String guessLocation(TournamentResults results) {
    if (results.events().isEmpty()) {
      return "";
    }
    if (results.tournamentName().contains("kennedy")) {
      return KENNEDYS;
    }
    LocalDate date = LocalDate.parse(results.events().get(0).date());
    // Thursdays were always in SF.
    if (date.getDayOfWeek() == DayOfWeek.THURSDAY) {
      return KENNEDYS;
    }
    // Pandemic closed other locations
    if (date.getYear() >= 2019) {
      return CALIFORNIA_BILLIARDS;
    }
    // Cal billiards reopened in Fremont in 2016, we moved Tuesday events there.
    if (date.getYear() >= 2016 && date.getDayOfWeek() == DayOfWeek.TUESDAY) {
      return CALIFORNIA_BILLIARDS;
    }
    return "";
  }

  private static String getPlayers(TournamentResults.EventResults event) {
    Set<String> players = Sets.newHashSet();
    for (TournamentResults.Finish finish : event.finishes()) {
      players.add(finish.playerOne());
      if (finish.playerTwo() != null) {
        players.add(finish.playerTwo());
      }
    }
    return formatPlayers(players);
  }

  private static String formatPlayers(Set<String> players) {
    return Joiner.on(',').join(players);
  }
}
