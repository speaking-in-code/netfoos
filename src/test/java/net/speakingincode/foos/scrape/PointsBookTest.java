package net.speakingincode.foos.scrape;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class PointsBookTest {
  @Test
  public void readFromString() throws IOException {
    PointsBook book = PointsBook.loadFromString("{\"players\":["
        + "{\"name\":\"Spredeman, Tony\",\"points\":6740,\"local\":0}]}");
    PointsBookPlayer expected = new PointsBookPlayer();
    expected.setName("Spredeman, Tony");
    expected.setPoints(6740);
    expected.setLocal(0);
    assertThat(book.getPointsBook().values(), contains(expected));
    assertThat(book.getPointsBook().get("Spredeman, Tony"), equalTo(expected));
  }
  
  @Test
  public void updatesLocal() throws IOException {
    PointsBook book = PointsBook.loadFromString("{\"players\":["
        + "{\"name\":\"Spredeman, Tony\",\"points\":6740,\"local\":0}]}");
    Player tony = Player.builder()
        .name("Spredeman, Tony")
        .oldPoints(6740)
        .oldBasePoints(7000)
        .newBasePoints(7000)
        .newPoints(7000)
        .build();
    PointsBookData data = book.getUpdate(ImmutableList.of(tony));
    PointsBookPlayer expected = new PointsBookPlayer();
    expected.setName("Spredeman, Tony");
    expected.setPoints(7000);
    expected.setLocal(1);
    assertThat(data.getPlayers(), contains(expected));
  }
  
  @Test
  public void updatesOrder() throws IOException {
    PointsBook book = PointsBook.loadFromString("{\"players\":["
        + "{\"name\":\"Spredeman, Tony\",\"points\":6740,\"local\":0},"
        + "{\"name\":\"Loffredo, Todd\",\"points\":5000,\"local\":0}"
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
    PointsBookPlayer expectedTony = new PointsBookPlayer();
    expectedTony.setName("Spredeman, Tony");
    expectedTony.setPoints(6740);
    expectedTony.setLocal(0);
    PointsBookPlayer expectedTodd = new PointsBookPlayer();
    expectedTodd.setName("Loffredo, Todd");
    expectedTodd.setPoints(7000);
    expectedTodd.setLocal(1);
    assertThat(data.getPlayers(), contains(expectedTodd, expectedTony));

  }
}
