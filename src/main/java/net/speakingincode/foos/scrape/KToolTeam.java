package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.util.List;

@AutoValue
public abstract class KToolTeam {
    public abstract String id();
    public abstract List<Player> players();

    public static KToolTeam fromJson(String text) {
        return GsonUtil.gson().fromJson(text, KToolTeam.class);
    }

    public static Builder builder() {
        return new AutoValue_KToolTeam.Builder();
    }

    public static TypeAdapter<KToolTeam> typeAdapter(Gson gson) {
        return new AutoValue_KToolTeam.GsonTypeAdapter(gson);
    }

    @AutoValue.Builder
    abstract static class Builder {
        public abstract Builder id(String s);
        public abstract Builder players(List<Player> l);
        public abstract KToolTeam build();
    }

    @AutoValue
    public abstract static class Player {
        public abstract String id();

        public static Player create(String id) {
            return new AutoValue_KToolTeam_Player(id);
        }

        public static TypeAdapter<Player> typeAdapter(Gson gson) {
            return new AutoValue_KToolTeam_Player.GsonTypeAdapter(gson);
        }
    }
}
