package net.speakingincode.foos.scrape;

import static org.junit.Assert.*;

import org.junit.Test;

public class PointsBookPlayerTest {

  @Test
  public void defaultBehavior() {
    PointsBookPlayer p = PointsBookPlayer.builder().setName("name").build();
    assertEquals("name", p.name());
    assertEquals(null, p.ifpId());
    assertEquals(null, p.local());
    assertEquals(null, p.points());
  }
  
  @Test
  public void allFields() {
    PointsBookPlayer p = PointsBookPlayer.builder()
        .setName("name")
        .setPoints(100)
        .setLocal(200)
        .setIfpId("ifp name")
        .build();
    assertEquals("name", p.name());
    assertEquals("ifp name", p.ifpId());
    assertEquals(200, p.local().intValue());
    assertEquals(100, p.points().intValue());
  }
}
