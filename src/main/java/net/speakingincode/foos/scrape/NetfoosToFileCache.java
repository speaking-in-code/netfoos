package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.util.List;

@AutoValue
public abstract class NetfoosToFileCache {
  public abstract List<NetfoosToFileMetadata> events();

  public static NetfoosToFileCache create(List<NetfoosToFileMetadata> events) {
    return new AutoValue_NetfoosToAirtableCache(events);
  }

  public static TypeAdapter<NetfoosToFileCache> typeAdapter(Gson gson) {
    return new AutoValue_NetfoosToAirtableCache.GsonTypeAdapter(gson);
  }
}
