package net.speakingincode.foos.scrape;

import com.google.common.collect.ImmutableMap;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Used for handling short player names in Monster DYP results files. Maps from short names to full name stored in
 * .netfoosrc.
 */
public class NameMap {
    public static NameMap load() {
        try (FileReader reader = new FileReader(Credentials.getNetfoosRcPath())) {
            Properties properties = new Properties();
            properties.load(reader);
            return new NameMap(properties);
        } catch (IOException e) {
            throw new RuntimeException("Error loading credentials", e);
        }
    }

    private final ImmutableMap<String, String> map;

    private NameMap(Properties properties) {
        ImmutableMap.Builder<String, String> b = ImmutableMap.builder();
        for (String name : properties.stringPropertyNames()) {
            if ("username".equals(name) || "password".equals(name)) {
                continue;
            }
            String fullName = properties.getProperty(name);
            b.put(name, fullName);
        }
        map = b.build();
    }

    public String fullName(String name) {
        String fullName = map.get(name);
        return fullName != null ? fullName : name;
    }
}
