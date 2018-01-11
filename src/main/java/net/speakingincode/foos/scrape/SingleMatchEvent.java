package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SingleMatchEvent {
  public static Builder builder() {
    return new AutoValue_SingleMatchEvent.Builder();
  }
  public abstract String tournamentId();
  public abstract String winnerPlayerOne();
  public abstract String winnerPlayerTwo();
  public abstract String loserPlayerOne();
  public abstract String loserPlayerTwo();
  
  @AutoValue.Builder
  public static abstract class Builder {
    public abstract Builder tournamentId(String tournamentId);
    public abstract Builder winnerPlayerOne(String winnerPlayerOne);
    public abstract Builder winnerPlayerTwo(String winnerPlayerTwo);
    public abstract Builder loserPlayerOne(String loserPlayerOne);
    public abstract Builder loserPlayerTwo(String loserPlayerTwo);
    public abstract SingleMatchEvent build();
  }
}
