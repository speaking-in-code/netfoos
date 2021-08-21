package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class KToolTeam {
    @SerializedName(value="id", alternate={"_id"})
    public abstract String id();
    public abstract @Nullable String name();
    abstract @Nullable String type();
    @SerializedName(value="players")
    abstract @Nullable List<Player> teamPlayers();

    // v3 of the format uses a singles team format like this:
    // "team1": {
    //   "_id": "Sy4nvyGjm",
    //   "type": "Player"
    // },
    @Memoized
    public @Nullable List<Player> players() {
        if ("Player".equals(type())) {
            return ImmutableList.of(Player.create(id()));
        }
        return teamPlayers();
    }

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
        abstract Builder type(String s);
        abstract Builder teamPlayers(List<Player> l);
        public abstract KToolTeam build();
    }

    @AutoValue
    public abstract static class Player {
        @SerializedName(value="id", alternate={"_id"})
        public abstract String id();

        public static Player create(String id) {
            return new AutoValue_KToolTeam_Player(id);
        }

        public static TypeAdapter<Player> typeAdapter(Gson gson) {
            return new AutoValue_KToolTeam_Player.GsonTypeAdapter(gson);
        }
    }
}
