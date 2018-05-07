package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;

@AutoValue
public abstract class ResultsParserConfig {
    public abstract @Nullable InputStream ktool();
    public abstract @Nullable InputStream metadata();
    public abstract @Nullable File matches();

    public static Builder builder() {
        return new AutoValue_ResultsParserConfig.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ktool(@Nullable InputStream s);
        public abstract Builder metadata(@Nullable InputStream s);
        public abstract Builder matches(@Nullable File matches);
        abstract ResultsParserConfig autoBuild();
        public ResultsParserConfig build() {
            ResultsParserConfig c = autoBuild();
            Preconditions.checkState(c.ktool() != null || c.matches() != null,
                "Must specify either ktool or matches config files");
            Preconditions.checkState(c.ktool() == null || c.matches() == null,
                "Cannot specify both ktool and matches config files");
            Preconditions.checkState(c.ktool() == null || c.metadata() != null,
                "Must specify ktool and metadata file together");
            Preconditions.checkState(c.metadata() == null || c.ktool() != null,
                "Must specify ktool and metadata file together");
            return c;
        }
    }
}
