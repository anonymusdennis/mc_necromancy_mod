package com.sirolf2009.necromancy.bodypart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class BodypartDefinitionIo {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private BodypartDefinitionIo() {}

    public static String toJson(BodypartDefinitionJson def) {
        return GSON.toJson(def);
    }

    public static BodypartDefinitionJson fromJson(String raw) {
        return GSON.fromJson(raw, BodypartDefinitionJson.class);
    }
}
