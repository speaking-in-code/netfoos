package net.speakingincode.foos.scrape;

import java.time.LocalDate;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Tournament {
  public abstract String name();
  public abstract LocalDate date();
  public abstract String description();
  public abstract String location();
  public abstract String address();
  public abstract String city();
  public abstract String state();
  public abstract String zip();
  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_Tournament.Builder();
  }
  
  @AutoValue.Builder
  public static abstract class Builder {
    public abstract Builder setName(String name);
    public abstract Builder setDate(LocalDate date);
    public abstract Builder setDescription(String description);
    public abstract Builder setLocation(String location);
    public abstract Builder setAddress(String address);
    public abstract Builder setCity(String city);
    public abstract Builder setState(String state);
    public abstract Builder setZip(String zip);
    public abstract Tournament build();
  }
}
