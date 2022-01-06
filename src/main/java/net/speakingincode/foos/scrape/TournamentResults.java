package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Results from a tournament.
 * <p>
 * Data Model:
 * events [0..n]
 * tournament name
 * date
 * event name
 * finishes [0..n]
 */
@AutoValue
public abstract class TournamentResults {
  public static Builder builder() {
    return new AutoValue_TournamentResults.Builder();
  }

  public abstract String tournamentId();

  public abstract String tournamentName();

  public abstract @Nullable
  List<EventResults> events();

  public static TypeAdapter<TournamentResults> typeAdapter(Gson gson) {
    return new AutoValue_TournamentResults.GsonTypeAdapter(gson);
  }

  @AutoValue.Builder
  public static abstract class Builder {
    public abstract Builder tournamentId(String tournamentId);

    public abstract Builder tournamentName(String tournamentName);

    public abstract Builder events(List<EventResults> l);

    public abstract TournamentResults build();
  }

  @AutoValue
  public static abstract class Finish {
    public static Builder builder() {
      return new AutoValue_TournamentResults_Finish.Builder();
    }

    public abstract int finish();

    public abstract String playerOne();

    public abstract @Nullable
    String playerTwo();

    public static TypeAdapter<Finish> typeAdapter(Gson gson) {
      return new AutoValue_TournamentResults_Finish.GsonTypeAdapter(gson);
    }

    @AutoValue.Builder
    public abstract static class Builder {
      public abstract Finish build();

      public abstract Builder finish(int i);

      public abstract Builder playerOne(String s);

      public abstract Builder playerTwo(@Nullable String s);
    }
  }

  @AutoValue
  public static abstract class EventResults {
    public abstract String tournamentName();
    public abstract String date();
    public abstract String eventName();
    public abstract String chartUrl();
    @SerializedName(value = "finishes")
    protected abstract List<Finish> finishesInternal();

    private static final Ordering<Finish> byFinish = new Ordering<Finish>() {
      @Override
      public int compare(Finish left, Finish right) {
        return Integer.compare(left.finish(), right.finish());
      }
    };

    @Memoized
    public List<Finish> finishes() {
      return byFinish.immutableSortedCopy(finishesInternal());
    }

    public static TypeAdapter<EventResults> typeAdapter(Gson gson) {
      return new AutoValue_TournamentResults_EventResults.GsonTypeAdapter(gson);
    }

    public static Builder builder() {
      return new AutoValue_TournamentResults_EventResults.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
      public abstract Builder tournamentName(String s);
      public abstract Builder date(String s);
      public abstract Builder eventName(String s);
      public abstract Builder chartUrl(String s);
      public abstract Builder finishesInternal(List<Finish> l);
      public abstract EventResults build();
    }
  }
}
