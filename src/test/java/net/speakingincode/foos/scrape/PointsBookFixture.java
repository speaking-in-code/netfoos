package net.speakingincode.foos.scrape;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import java.io.IOException;

public class PointsBookFixture {
  private PointsBookFixture() {
  }

  public static PointsBook emptyPointsBook() {
    try {
      return PointsBook.loadFromString("{\"players\":[]}");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static PointsBook forPlayers(PointsBookPlayer... players) {
    PointsBookData data = new PointsBookData();
    data.setPlayers(ImmutableList.copyOf(players));
    return PointsBook.loadFromData(data);
  }
}
