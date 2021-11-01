package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;


@AutoValue
public abstract class IfpPlayer {
  public abstract String name();
  public abstract int doubles();
  public abstract boolean isActive();

  public static TypeAdapter<IfpPlayer> typeAdapter(Gson gson) {
    return new AutoValue_IfpPlayer.GsonTypeAdapter(gson);
  }
}
