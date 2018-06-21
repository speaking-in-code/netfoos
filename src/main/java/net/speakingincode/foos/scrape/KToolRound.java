package net.speakingincode.foos.scrape;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class KToolRound {
    public static TypeAdapter<KToolRound> typeAdapter(Gson gson) {
        return new AutoValue_KToolRound.GsonTypeAdapter(gson);
    }

    @SerializedName("plays")
    public abstract List<KToolPlay> plays();
}
