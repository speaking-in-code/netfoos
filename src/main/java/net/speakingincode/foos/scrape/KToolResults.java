package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.List;

@AutoValue
public abstract class KToolResults {
    public static KToolResults fromJson(String text) {
        return GsonUtil.gson().fromJson(text, KToolResults.class);
    }

    public abstract String created();
    public abstract List<KToolPlayer> players();
    public abstract List<KToolTeam> teams();
    public abstract List<KToolPlay> plays();
    public abstract KnockOut ko();

    @VisibleForTesting
    public static LocalDate getLocalDate(String textDate, ZoneId localZone) {
        ZonedDateTime utc = ZonedDateTime.parse(textDate, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        LocalDateTime local = LocalDateTime.ofInstant(utc.toInstant(), localZone);
        return local.toLocalDate();
    }

    public LocalDate createdDate() {
        return getLocalDate(created(), ZoneId.systemDefault());
    }

    public static KToolResults create(String created, List<KToolPlayer> players, List<KToolTeam> teams,
                                      List<KToolPlay> plays, KnockOut ko) {
        return new AutoValue_KToolResults(created, players, teams, plays, ko);
    }

    public static TypeAdapter<KToolResults> typeAdapter(Gson gson) {
        return new AutoValue_KToolResults.GsonTypeAdapter(gson);
    }

    @AutoValue
    public abstract static class KnockOut {
        public abstract List<Level> levels();

        public static KnockOut create(List<Level> l) {
            return new AutoValue_KToolResults_KnockOut(l);
        }

        public static TypeAdapter<KnockOut> typeAdapter(Gson gson) {
            return new AutoValue_KToolResults_KnockOut.GsonTypeAdapter(gson);
        }
    }

    @AutoValue
    public abstract static class Level {
        public abstract List<KToolPlay> plays();

        public static Level create(List<KToolPlay> l) {
            return new AutoValue_KToolResults_Level(l);
        }

        public static TypeAdapter<Level> typeAdapter(Gson gson) {
            return new AutoValue_KToolResults_Level.GsonTypeAdapter(gson);
        }
    }
}
