package net.speakingincode.foos.scrape;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class IfpPoints {
  private static final int INACTIVE_POINTS = 1200;

  public static IfpPoints load() throws IOException {
    try (InputStream testInput = IfpPoints.class.getResourceAsStream("/points.json")) {
      String json = CharStreams.toString(new InputStreamReader(testInput, Charsets.UTF_8));
      IfpPlayer[] players = GsonUtil.gson().fromJson(json, IfpPlayer[].class);
      return new IfpPoints(players);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final ImmutableMap<String, IfpPlayer> players;

  public IfpPoints(IfpPlayer[] playerList) {
    // There are dupe player names, from different states. Should probably use IFP unique id as key, or
    // maybe player + state.
    Map<String, IfpPlayer> b = Maps.newHashMap();
    for (IfpPlayer player : playerList) {
      b.put(player.name().toLowerCase(), player);
    }
    players = ImmutableMap.copyOf(b);
  }

  public Integer getPoints(String name) {
    IfpPlayer player = players.get(name.toLowerCase());
    if (player == null) {
      return null;
    }
    if (!player.isActive()) {
      return INACTIVE_POINTS;
    }
    return player.doubles();
  }
}

