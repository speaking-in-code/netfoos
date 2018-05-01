package net.speakingincode.foos.scrape;

import java.time.LocalDate;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

@AutoValue
public abstract class Tournament {
  public static Tournament fromJson(String text) {
    return GsonUtil.gson().fromJson(text, Tournament.class);
  }

  public enum OutputFormatType {
    @SerializedName("team") TEAM,
    @SerializedName("individual") INDIVIDUAL
  }

  public abstract String name();
  public abstract @Nullable LocalDate date();
  public abstract String description();
  public abstract String location();
  public abstract String address();
  public abstract String city();
  public abstract String state();
  public abstract String zip();
  public abstract @Nullable String defaultKValue();
  public abstract OutputFormatType outputFormat();
  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_Tournament.Builder();
  }
  
  @AutoValue.Builder
  public static abstract class Builder {
    public abstract Builder setName(String name);
    public abstract Builder setDate(@Nullable LocalDate date);
    public abstract Builder setDescription(String description);
    public abstract Builder setLocation(String location);
    public abstract Builder setAddress(String address);
    public abstract Builder setCity(String city);
    public abstract Builder setState(String state);
    public abstract Builder setZip(String zip);
    public abstract Builder setDefaultKValue(@Nullable String kValue);
    public abstract Builder setOutputFormat(OutputFormatType outputFormat);
    public abstract Tournament build();
  }

  public static TypeAdapter<Tournament> typeAdapter(Gson gson) {
    return new AutoValue_Tournament.GsonTypeAdapter(gson);
  }
}
