package net.speakingincode.foos.scrape;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Used for handling short player names in Monster DYP results files. Maps from short names to full name stored in
 * .netfoosrc.
 */
public class NameMap {
  private static final Gson gson = new GsonBuilder().create();
  private static final Type mapType = new TypeToken<Map<String, String>>() {
  }.getType();

  public static NameMap load() {
    try (BufferedReader reader = Files.newBufferedReader(
        Paths.get(PreferenceFiles.getNameMapPath()), StandardCharsets.UTF_8)) {
      Map<String, String> names = gson.fromJson(reader, mapType);
      return new NameMap(names);
    } catch (IOException e) {
      throw new RuntimeException("Error loading credentials", e);
    }
  }

  private final ImmutableMap<String, String> map;

  private NameMap(Map<String, String> in) {
    ImmutableMap.Builder<String, String> b = ImmutableMap.builder();
    for (Map.Entry<String, String> item : in.entrySet()) {
      b.put(item.getKey().toLowerCase(), item.getValue());
    }
    map = b.build();
  }

  public String fullName(@Nullable String name) {
    if (name == null) {
      return null;
    }
    String fullName = map.get(name.toLowerCase());
    return fullName != null ? fullName : name;
  }
}
