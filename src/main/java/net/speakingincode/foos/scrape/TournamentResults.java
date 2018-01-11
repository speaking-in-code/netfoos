package net.speakingincode.foos.scrape;

import java.util.List;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

/**
 * Results from a tournament.
 * 
 * Data Model:
 *   events [0..n]
 *     tournament name
 *     date
 *     event name
 *     finishes [0..n]
 */
@AutoValue
public abstract class TournamentResults {
  public static Builder builder() {
    return new AutoValue_TournamentResults.Builder();
  }
  public abstract String tournamentId();
  public abstract ImmutableList<EventResults> events();
  
  @AutoValue.Builder
  public static abstract class Builder {
    private final List<EventResults> results = Lists.newArrayList();
    
    public abstract Builder tournamentId(String tournamentId);
    
    public Builder addEvent(EventResults result) {
      results.add(result);
      return this;
    }
    
    protected abstract Builder events(ImmutableList<EventResults> l);
    
    public TournamentResults build() {
      return events(ImmutableList.copyOf(results)).autoBuild();
    }
    
    public abstract TournamentResults autoBuild();
  }
  
  @AutoValue
  public static abstract class Finish {
    public static Builder builder() {
      return new AutoValue_TournamentResults_Finish.Builder();
    }
    public abstract int finish();
    public abstract String playerOne();
    public abstract @Nullable String playerTwo();
    
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
    public abstract ImmutableList<Finish> finishes();
    
    public static Builder builder() {
      return new AutoValue_TournamentResults_EventResults.Builder();
    }
    
    @AutoValue.Builder
    public abstract static class Builder {
      private List<Finish> unorderedFinishes = Lists.newArrayList();
      
      
      public abstract Builder tournamentName(String s);
      
      public abstract Builder date(String s);
      
      public abstract Builder eventName(String s);
      
      public abstract Builder chartUrl(String s);
      
      public Builder addFinish(Finish f) {
        unorderedFinishes.add(f);
        return this;
      }
      protected abstract Builder finishes(ImmutableList<Finish> l);
      public EventResults build() {
        finishes(byFinish.immutableSortedCopy(unorderedFinishes));
        return autoBuild();
      }
      
      public abstract EventResults autoBuild();
      
      private static final Ordering<Finish> byFinish = new Ordering<Finish>() {
        @Override
        public int compare(Finish left, Finish right) {
          return Integer.compare(left.finish(), right.finish());
        }
      };
    }
  }
}
