package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.ryanharter.auto.value.gson.AutoValueGsonBuilder;

import javax.annotation.Nullable;

@AutoValue
public abstract class PointsBookPlayer {
  public abstract String name();

  public abstract @Nullable
  Integer points();

  public abstract @Nullable
  Integer local();

  public abstract @Nullable
  String ifpId();

  public abstract @Nullable
  Integer ifpActive();

  public abstract Builder toBuilder();

  @AutoValueGsonBuilder
  public static Builder builder() {
    return new AutoValue_PointsBookPlayer.Builder()
        .setIfpActive(0)
        .setIfpId("");
  }

  @AutoValue.Builder
  abstract static class Builder {
    public abstract Builder setName(String name);

    public abstract Builder setPoints(Integer points);

    public abstract Builder setLocal(Integer localPoints);

    public abstract Builder setIfpId(String ifpId);

    public abstract Builder setIfpActive(Integer ifpActive);

    public abstract PointsBookPlayer build();
  }

  public static TypeAdapter<PointsBookPlayer> typeAdapter(Gson gson) {
    return new AutoValue_PointsBookPlayer.GsonTypeAdapter(gson);
  }
}
