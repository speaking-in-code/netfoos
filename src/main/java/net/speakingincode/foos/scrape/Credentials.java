package net.speakingincode.foos.scrape;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Credentials {
	private final String username;
	private final String password;
	
	public static Credentials load() {
	  try (FileReader reader = new FileReader(getNetfoosRcPath())) {
	    Properties properties = new Properties();
	    properties.load(reader);
	    String username = properties.getProperty("username");
	    String password = properties.getProperty("password");
	    if (username == null || password == null) {
	      throw new RuntimeException("Couldn't find username or password in .netfoosrc");
	    }
	    return new Credentials(username, password);
	  } catch (IOException e) {
	    throw new RuntimeException("Error laoding credentials", e);
	  }
	}
	
	private static String getNetfoosRcPath() {
    return System.getenv("HOME") + "/.netfoosrc";
  }

  public static Credentials create(String username, String password) {
		return new Credentials(username, password);
	}
	
	private Credentials(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String username() {
		return username;
	}
	
	public String password() {
		return password;
	}
}
