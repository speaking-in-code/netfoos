package net.speakingincode;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
public abstract class Player {
  public static Builder builder() {
    return new AutoValue_Player.Builder();
  }
  public abstract String name();
  public abstract @Nullable String currentPoints();
  public abstract String newPoints();
  
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder name(String name);
    public abstract Builder currentPoints(@Nullable String s);
    public abstract Builder newPoints(String s);
    public abstract Player build();
  }
}
