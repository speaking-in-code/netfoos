package net.speakingincode;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Credentials {
	private final String username;
	private final String password;
	
	public static Credentials load() throws IOException {
		String home = System.getenv("HOME");
		Properties properties = new Properties();
		properties.load(new FileReader(home + "/" + ".netfoosrc"));
		String username = properties.getProperty("username");
		String password = properties.getProperty("password");
		if (username == null || password == null) {
			throw new IOException("Couldn't find username or password in .netfoosrc");
		}
		return new Credentials(username, password);
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
