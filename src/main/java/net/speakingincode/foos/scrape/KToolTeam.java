package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class KToolTeam {
    public abstract String id();
    public abstract @Nullable String name();
    public abstract List<Player> players();

    public List<String> teamPlayerNames() {
        Preconditions.checkState(players().isEmpty(), "Use players list instead");
        Preconditions.checkState(name() != null, "Player names not in team name");
        return teamToPlayers(name());
    }

    public static List<String> teamToPlayers(String teamName) {
        String[] names = teamName.split("/", 2);
        return ImmutableList.copyOf(names);
    }

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
        public abstract Builder name(String s);
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
