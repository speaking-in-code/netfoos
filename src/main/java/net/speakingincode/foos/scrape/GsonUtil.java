package net.speakingincode.foos.scrape;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class GsonUtil {

    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(MyAdapterFactory.create())
        .registerTypeAdapter(KToolResults.knockOutWrapperType, new KToolResults.KnockOutWrapperAdapter())
        .setPrettyPrinting()
        .create();

    public static Gson gson() {
        return gson;
    }
}

