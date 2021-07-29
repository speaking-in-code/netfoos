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
  public static IfpPoints load() throws IOException {
    try (InputStream testInput = IfpPoints.class.getResourceAsStream("/points.json")) {
      String json = CharStreams.toString(new InputStreamReader(testInput, Charsets.UTF_8));
      IfpPlayer[] players = GsonUtil.gson().fromJson(json, IfpPlayer[].class);
      return new IfpPoints(players);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final ImmutableMap<String, Integer> players;

  public IfpPoints(IfpPlayer[] playerList) {
    Map<String, Integer> b = Maps.newHashMap();
    for (IfpPlayer player: playerList) {
      b.put(player.name().toLowerCase(), player.doubles());
    }
    players = ImmutableMap.copyOf(b);
  }

  public Integer getPoints(String name) {
    return players.get(name.toLowerCase());
  }
}

