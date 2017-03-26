package net.speakingincode.foos.scrape;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class PointsBookPlayer {
  public abstract String name();
  public abstract @Nullable Integer points();
  public abstract @Nullable Integer local();
  public abstract @Nullable String ifpId();
  public abstract Builder toBuilder();

  static Builder builder() {
    return new AutoValue_PointsBookPlayer.Builder();
  }
  
  @AutoValue.Builder
  abstract static class Builder {
    public abstract Builder setName(String name);
    public abstract Builder setPoints(Integer points);
    public abstract Builder setLocal(Integer localPoints);
    public abstract Builder setIfpId(String ifpId);
    public abstract PointsBookPlayer build();
  }
  
  public static TypeAdapter<PointsBookPlayer> typeAdapter(Gson gson) {
    return new AutoValue_PointsBookPlayer.GsonTypeAdapter(gson);
  }
}