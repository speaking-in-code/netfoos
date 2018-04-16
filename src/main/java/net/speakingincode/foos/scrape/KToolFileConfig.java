package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;

import java.io.InputStream;

@AutoValue
public abstract class KToolFileConfig {
    public abstract InputStream ktool();
    public abstract InputStream metadata();

    public static Builder builder() {
        return new AutoValue_KToolFileConfig.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ktool(InputStream s);
        public abstract Builder metadata(InputStream s);
        public abstract KToolFileConfig build();
    }
}
