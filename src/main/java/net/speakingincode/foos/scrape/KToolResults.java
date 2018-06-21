package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import javax.annotation.Nullable;
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

    @Memoized
    public List<KToolPlay> plays() {
        if (v1plays() != null) {
            return v1plays();
        }
        ImmutableList.Builder<KToolPlay> b = ImmutableList.builder();
        for (KToolRound round : v2rounds()) {
            b.addAll(round.plays());
        }
        return b.build();
    }

    /**
     * v1 of the file format has plays as a list.
     */
    @SerializedName("plays")
    abstract @Nullable List<KToolPlay> v1plays();

    /**
     * v2 of the file format has a list of rounds, with plays internal.
     */
    @SerializedName("rounds")
    abstract @Nullable List<KToolRound> v2rounds();

    @Memoized
    public @Nullable KnockOut ko() {
        if (koWrapper() == null) {
            return null;
        }
        return koWrapper().ko();
    }

    @SerializedName("ko")
    abstract @Nullable KnockOutWrapper koWrapper();

    @VisibleForTesting
    public static LocalDate getLocalDate(String textDate, ZoneId localZone) {
        ZonedDateTime utc = ZonedDateTime.parse(textDate, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        LocalDateTime local = LocalDateTime.ofInstant(utc.toInstant(), localZone);
        return local.toLocalDate();
    }

    public LocalDate createdDate() {
        return getLocalDate(created(), ZoneId.systemDefault());
    }

    public static TypeAdapter<KToolResults> typeAdapter(Gson gson) {
        return new AutoValue_KToolResults.GsonTypeAdapter(gson);
    }

    @AutoValue
    public abstract static class KnockOut {
        public abstract List<Level> levels();
        public abstract Level third();

        public static KnockOut create(List<Level> l, Level t) {
            return new AutoValue_KToolResults_KnockOut(l, t);
        }

        public static TypeAdapter<KnockOut> typeAdapter(Gson gson) {
            return new AutoValue_KToolResults_KnockOut.GsonTypeAdapter(gson);
        }
    }

    @AutoValue
    public abstract static class KnockOutWrapper {
        public abstract @Nullable KnockOut ko();
        public static KnockOutWrapper create(KnockOut ko) {
            return new AutoValue_KToolResults_KnockOutWrapper(ko);
        }
    }

    static final Type knockOutWrapperType = new TypeToken<KnockOutWrapper>(){}.getType();

    static class KnockOutWrapperAdapter implements JsonDeserializer<KnockOutWrapper> {
        public KnockOutWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            KnockOut ko = null;
            if (json.isJsonArray()) {
                JsonElement first = json.getAsJsonArray().get(0);
                ko = ctx.deserialize(first, KnockOut.class);
            } else if (json.isJsonObject()) {
                ko = ctx.deserialize(json, KnockOut.class);
            } else {
                throw new RuntimeException("Unexpected JSON type: " + json.getClass());
            }
            return KnockOutWrapper.create(ko);
        }
    }

    @AutoValue
    public abstract static class Level {
        public abstract List<KToolPlay> plays();
        public abstract String name();

        public static Level create(List<KToolPlay> l, String n) {
            return new AutoValue_KToolResults_Level(l, n);
        }

        public static TypeAdapter<Level> typeAdapter(Gson gson) {
            return new AutoValue_KToolResults_Level.GsonTypeAdapter(gson);
        }
    }
}
