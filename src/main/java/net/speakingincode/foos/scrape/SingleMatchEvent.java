package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class SingleMatchEvent {
  public static Builder builder() {
    return new AutoValue_SingleMatchEvent.Builder()
        .tie(false);
  }
  public abstract Builder toBuilder();
  public abstract String kValue();
  public abstract String winnerPlayerOne();
  public abstract @Nullable String winnerPlayerTwo();
  public abstract String loserPlayerOne();
  public abstract @Nullable String loserPlayerTwo();
  public abstract boolean tie();

  @AutoValue.Builder
  public static abstract class Builder {
    public abstract Builder kValue(String kValue);
    public abstract Builder winnerPlayerOne(String winnerPlayerOne);
    public abstract Builder winnerPlayerTwo(@Nullable String winnerPlayerTwo);
    public abstract Builder loserPlayerOne(String loserPlayerOne);
    public abstract Builder loserPlayerTwo(@Nullable String loserPlayerTwo);
    public abstract Builder tie(boolean tie);
    public abstract SingleMatchEvent build();
  }
}
