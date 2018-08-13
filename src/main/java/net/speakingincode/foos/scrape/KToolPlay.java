package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class KToolPlay {
    abstract boolean valid();
    public abstract Team team1();
    public abstract @Nullable Team team2();
    public abstract @Nullable List<Match> disciplines();

    public boolean matchWasPlayed() {
        return valid() && team2() != null;
    }

    public static KToolPlay create(boolean valid, Team t1, Team t2, List<Match> disciplines) {
        return new AutoValue_KToolPlay(valid, t1, t2, disciplines);
    }

    public static TypeAdapter<KToolPlay> typeAdapter(Gson gson) {
        return new AutoValue_KToolPlay.GsonTypeAdapter(gson);
    }

    @AutoValue
    public abstract static class Team {
        public abstract String id();

        public static Team create(String id) {
            return new AutoValue_KToolPlay_Team(id);
        }

        public static TypeAdapter<Team> typeAdapter(Gson gson) {
            return new AutoValue_KToolPlay_Team.GsonTypeAdapter(gson);
        }
    }

    @AutoValue
    public abstract static class Match {
        public abstract List<Set> sets();

        public static Match create(List<Set> sets) {
            return new AutoValue_KToolPlay_Match(sets);
        }

        public static TypeAdapter<Match> typeAdapter(Gson gson) {
            return new AutoValue_KToolPlay_Match.GsonTypeAdapter(gson);
        }
    }

    @AutoValue
    public abstract static class Set {
        public abstract int team1();
        public abstract int team2();

        public static Set create(int team1, int team2) {
            return new AutoValue_KToolPlay_Set(team1, team2);
        }

        public static TypeAdapter<Set> typeAdapter(Gson gson) {
            return new AutoValue_KToolPlay_Set.GsonTypeAdapter(gson);
        }
    }
}
