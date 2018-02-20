package net.speakingincode.foos.scrape;

public class PreferenceFiles {
    private static String getHome() {
        return System.getenv("HOME");
    }

    public static String getNetfoosRcPath() {
        return getHome() + "/.netfoosrc";
    }

    public static String getNameMapPath() {
        return getHome() + "/.netfoosnames";
    }
}
