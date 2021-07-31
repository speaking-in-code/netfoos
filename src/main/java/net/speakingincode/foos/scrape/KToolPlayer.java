package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class KToolPlayer {
    public static KToolPlayer fromJson(String text) {
        return GsonUtil.gson().fromJson(text, KToolPlayer.class);
    }

    @SerializedName(value="id", alternate={"_id"})
    public abstract String id();

    @Memoized
    public String name() {
        return nameInternal().trim();
    }

    @SerializedName(value="name", alternate={"_name"})
    abstract String nameInternal();


    public static Builder builder() {
        return new AutoValue_KToolPlayer.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
        public abstract Builder id(String s);
        public abstract Builder nameInternal(String s);
        public abstract KToolPlayer build();
    }

    public static TypeAdapter<KToolPlayer> typeAdapter(Gson gson) {
        return new AutoValue_KToolPlayer.GsonTypeAdapter(gson);
    }
}
