package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@AutoValue
public abstract class MonsterResult {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tournament(Tournament t);
        public abstract Builder players(ImmutableSet<String> players);
        public abstract Builder matches(ImmutableList<SingleMatchEvent> matches);
        public abstract MonsterResult build();
    }

    public static Builder builder() {
        return new AutoValue_MonsterResult.Builder();
    }
    public abstract Tournament tournament();
    public abstract ImmutableSet<String> players();
    public abstract ImmutableList<SingleMatchEvent> matches();
}
