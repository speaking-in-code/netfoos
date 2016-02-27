package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;

import net.speakingincode.foos.scrape.AutoValue_Player;

@AutoValue
public abstract class Player {
  public static Builder builder() {
    return new AutoValue_Player.Builder();
  }
  
  public abstract Builder toBuilder();
  public abstract String name();
  public abstract int oldPoints();
  public abstract int newPoints();
  public abstract int oldBasePoints();
  public abstract int newBasePoints();
  
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder name(String name);
    public abstract Builder oldPoints(int i);
    public abstract Builder newPoints(int i);
    public abstract Builder oldBasePoints(int i);
    public abstract Builder newBasePoints(int i);
    public abstract Player build();
  }
}
