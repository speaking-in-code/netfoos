package net.speakingincode.foos.scrape;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.common.collect.Maps;

public class IfpCache {
  private static final File PLAYER_LIST_CACHE =
      new File(System.getenv("HOME") + "/.netfoosplayers");
  private static final Logger logger = Logger.getLogger(IfpCache.class.getName());
  
  public IfpCache() {}
  
  public Map<String, Integer> readPlayerTable() throws IOException {
    try (Reader reader = Files.newBufferedReader(PLAYER_LIST_CACHE.toPath())) {
      Properties props = new Properties();
      props.load(reader);
      Map<String, Integer> players = Maps.newHashMap();
      for (Entry<Object, Object> entry : props.entrySet()) {
        players.put(entry.getKey().toString(), Integer.parseInt(entry.getValue().toString()));
      }
      return players;
    }  
  }
  
  public void storePlayerTable(Map<String, Integer> map) throws IOException {
    Properties props = new Properties();
    for (Entry<String, Integer> entry : map.entrySet()) {
      props.setProperty(entry.getKey(), "" + entry.getValue());
    }
    logger.info("Writing player list cache to " + PLAYER_LIST_CACHE);
    try (Writer writer = Files.newBufferedWriter(PLAYER_LIST_CACHE.toPath())) {
      props.store(writer, "");
    }
  }
}
