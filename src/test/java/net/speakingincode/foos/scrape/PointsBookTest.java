package net.speakingincode.foos.scrape;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PointsBookTest {
  @Test
  public void readFromString() throws IOException {
    PointsBook book = PointsBook.loadFromString("{\"players\":["
        + "{\"name\":\"Spredeman, Tony\",\"points\":6740,\"local\":0,\"ifpActive\":1}]}");
    PointsBookPlayer expected = PointsBookPlayer.builder()
        .setName("Spredeman, Tony")
        .setPoints(6740)
        .setLocal(0)
        .setIfpActive(1)
        .build();
    assertThat(book.getPointsBook().values(), contains(expected));
    assertThat(book.getPointsBook().get("Spredeman, Tony"), equalTo(expected));
  }

  @Test
  public void updatesLocal() throws IOException {
    PointsBook book = PointsBook.loadFromString("{\"players\":["
        + "{\"name\":\"Spredeman, Tony\",\"points\":6740,\"local\":0,\"ifpActive\":1}]}");
    Player tony = Player.builder()
        .name("Spredeman, Tony")
        .oldPoints(6740)
        .oldBasePoints(7000)
        .newBasePoints(7000)
        .newPoints(7000)
        .build();
    PointsBookData data = book.getUpdate(ImmutableList.of(tony));
    PointsBookPlayer expected = PointsBookPlayer.builder()
        .setName("Spredeman, Tony")
        .setPoints(7000)
        .setLocal(0)
        .setIfpActive(1)
        .build();
    assertThat(data.getPlayers(), contains(expected));
  }

  @Test
  public void updatesOrder() throws IOException {
    PointsBook book = PointsBook.loadFromString("{\"players\":["
        + "{\"name\":\"Spredeman, Tony\",\"points\":6740,\"local\":0,\"ifpActive\":1},"
        + "{\"name\":\"Loffredo, Todd\",\"points\":5000,\"local\":0,\"ifpActive\":1}"
        + "]}");
    Player todd = Player.builder()
        .name("Loffredo, Todd")
        .oldPoints(5000)
        .oldBasePoints(1)
        .newBasePoints(1)
        .newPoints(7000)
        .build();
    Player tony = Player.builder()
        .name("Spredeman, Tony")
        .oldPoints(6740)
        .oldBasePoints(1)
        .newBasePoints(1)
        .newPoints(6740)
        .build();
    PointsBookData data = book.getUpdate(ImmutableList.of(todd, tony));
    PointsBookPlayer expectedTony = PointsBookPlayer.builder()
        .setName("Spredeman, Tony")
        .setPoints(6740)
        .setLocal(0)
        .setIfpActive(1)
        .build();
    PointsBookPlayer expectedTodd = PointsBookPlayer.builder()
        .setName("Loffredo, Todd")
        .setPoints(7000)
        .setLocal(0)
        .setIfpActive(1)
        .build();
    assertThat(data.getPlayers(), contains(expectedTodd, expectedTony));
  }
}
