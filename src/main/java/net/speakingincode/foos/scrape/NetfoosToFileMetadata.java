package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import javax.annotation.Nullable;

@AutoValue
public abstract class NetfoosToFileMetadata {
  public abstract @Nullable String tournamentName();
  public abstract @Nullable TournamentResults results();
  public abstract NetfoosToFileMetadata.Builder toBuilder();

  public static NetfoosToFileMetadata.Builder builder() {
    return new AutoValue_NetfoosToFileMetadata.Builder();
  }

  public static TypeAdapter<NetfoosToFileMetadata> typeAdapter(Gson gson) {
    return new AutoValue_NetfoosToFileMetadata.GsonTypeAdapter(gson);
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder tournamentName(String s);
    public abstract Builder results(TournamentResults s);
    public abstract NetfoosToFileMetadata build();
  }
}
