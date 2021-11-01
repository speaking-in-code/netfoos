package net.speakingincode.foos.scrape;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.util.Locale;

public class NetfoosTable {
  private final ImmutableMap<String, Player> players;

  public NetfoosTable(ImmutableMap<String, Player> players) {
    this.players = players;
  }

  public Player get(String name) {
    return players.get(name.toLowerCase(Locale.ROOT));
  }
}
