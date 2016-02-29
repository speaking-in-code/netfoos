package net.speakingincode.foos.scrape;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Splitter.MapSplitter;
import com.google.common.collect.ImmutableMultimap;

/**
 * Utility class for parsing URLs and query strings, because for some reason on one has added
 * a good library to the java core, and the apache interfaces are funky.
 * 
 * Incomplete and buggy as hell.
 */
public class ParsedUrl {
  public static ParsedUrl parse(String urlStr)
      throws MalformedURLException, UnsupportedEncodingException {
    return new ParsedUrl(urlStr);
  }
  
  private static final MapSplitter querySplitter = Splitter.on('&').withKeyValueSeparator('=');
  private final URL url;
  private final ImmutableMultimap<String, String> queryArgs;
  
  private ParsedUrl(String urlStr) throws MalformedURLException, UnsupportedEncodingException {
    url = new URL(urlStr);
    Map<String, String> args = querySplitter.split(url.getQuery());
    ImmutableMultimap.Builder<String, String> decoded = ImmutableMultimap.builder();
    for (Map.Entry<String, String> kvPair : args.entrySet()) {
      String key = decode(kvPair.getKey());
      String val = decode(kvPair.getValue());
      decoded.put(key, val);
    }
    queryArgs = decoded.build();
  }
  
  private String decode(String comp) throws UnsupportedEncodingException {
    return URLDecoder.decode(comp, Charsets.UTF_8.name());
  }
  
  public URL getUrl() {
    return url;
  }
  
  public ImmutableMultimap<String, String> getQueryArgs()  {
    return queryArgs;
  }
}
