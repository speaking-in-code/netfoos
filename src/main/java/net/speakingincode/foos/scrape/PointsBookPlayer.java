package net.speakingincode.foos.scrape;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;

class PointsBookPlayer {
  private String name;
  private @Nullable Integer points;
  private @Nullable Integer local;
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  
  public int getPoints() {
    return points;
  }
  public void setPoints(int points) {
    this.points = points;
  }
  
  public int getLocal() {
    return local;
  }
  public void setLocal(int local) {
    this.local = local;
  }
  
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PointsBookPlayer)) {
      return false;
    }
    PointsBookPlayer p = (PointsBookPlayer) o;
    return Objects.equals(name, p.name) &&
        Objects.equals(points, p.points) &&
        Objects.equals(local, p.local);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(name, points, local);
  }
  
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("points", points)
        .add("local", local)
        .toString();
  }
}