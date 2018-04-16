package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class KToolPlayer {
    public static KToolPlayer fromJson(String text) {
        return GsonUtil.gson().fromJson(text, KToolPlayer.class);
    }

    public abstract String id();
    public abstract String name();

    public static Builder builder() {
        return new AutoValue_KToolPlayer.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        public abstract Builder id(String s);
        public abstract Builder name(String s);
        public abstract KToolPlayer build();
    }

    public static TypeAdapter<KToolPlayer> typeAdapter(Gson gson) {
        return new AutoValue_KToolPlayer.GsonTypeAdapter(gson);
    }
}
