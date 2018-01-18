package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SingleMatchEvent {
  public static Builder builder() {
    return new AutoValue_SingleMatchEvent.Builder()
        .tie(false);
  }
  public abstract String tournamentId();
  public abstract String kValue();
  public abstract String winnerPlayerOne();
  public abstract String winnerPlayerTwo();
  public abstract String loserPlayerOne();
  public abstract String loserPlayerTwo();
  public abstract boolean tie();
  
  @AutoValue.Builder
  public static abstract class Builder {
    public abstract Builder tournamentId(String tournamentId);
    public abstract Builder kValue(String kValue);
    public abstract Builder winnerPlayerOne(String winnerPlayerOne);
    public abstract Builder winnerPlayerTwo(String winnerPlayerTwo);
    public abstract Builder loserPlayerOne(String loserPlayerOne);
    public abstract Builder loserPlayerTwo(String loserPlayerTwo);
    public abstract Builder tie(boolean tie);
    public abstract SingleMatchEvent build();
  }
}
