package com.holybuckets.farming.config.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Json for a single biome entry: the biome id and the growth modifier of each seed planted in it.
 */
public class CropBiomeMatrixJsonConfig {

    public static final String DEF_CONFIG_FILE_PATH = "config/HBCropBiomeMatrixConfig.json";

    public static final String COMMENT_KEY = "comment";
    public static final String COMMENT = "Define each object with a biome name. Inside it add 'minecraft:seed_item': growth speed modifier."
        + " 0 disables the crop in that biome, 2 grows twice as fast. Unlisted biome and seed pairings default to 1";

    private final String biomeId;
    private final Map<String, Float> modifiers;

    public CropBiomeMatrixJsonConfig(String biomeId, Map<String, Float> modifiers) {
        this.biomeId = biomeId;
        this.modifiers = new LinkedHashMap<>(modifiers);
    }

    public CropBiomeMatrixJsonConfig(String biomeId, JsonObject obj) {
        this.biomeId = biomeId;
        this.modifiers = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            Float value = asFloat(entry.getValue());
            if (value != null) modifiers.put(entry.getKey(), value);
        }
    }

    public String getBiomeId() {
        return biomeId;
    }

    /** Seed item id to growth modifier, in file order. */
    public Map<String, Float> getModifiers() {
        return Collections.unmodifiableMap(modifiers);
    }

    public JsonObject serialize() {
        JsonObject obj = new JsonObject();
        modifiers.forEach(obj::addProperty);
        return obj;
    }

    /** Every top level key holding an object is a biome; the comment and any other primitives are skipped. */
    public static List<CropBiomeMatrixJsonConfig> parse(JsonObject root) {
        List<CropBiomeMatrixJsonConfig> entries = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            if (!entry.getValue().isJsonObject()) continue;
            entries.add(new CropBiomeMatrixJsonConfig(entry.getKey(), entry.getValue().getAsJsonObject()));
        }
        return entries;
    }

    public static JsonObject toJsonObject(List<CropBiomeMatrixJsonConfig> entries) {
        JsonObject root = new JsonObject();
        root.addProperty(COMMENT_KEY, COMMENT);
        for (CropBiomeMatrixJsonConfig entry : entries) {
            root.add(entry.getBiomeId(), entry.serialize());
        }
        return root;
    }

    public static List<CropBiomeMatrixJsonConfig> defaultConfig() {
        List<CropBiomeMatrixJsonConfig> entries = new ArrayList<>();

        Map<String, Float> iceSpikes = new LinkedHashMap<>();
        iceSpikes.put("minecraft:wheat_seeds", 0f);
        iceSpikes.put("minecraft:glow_berries", 0.5f);
        entries.add(new CropBiomeMatrixJsonConfig("minecraft:ice_spikes", iceSpikes));

        Map<String, Float> plains = new LinkedHashMap<>();
        plains.put("minecraft:wheat_seeds", 1f);
        plains.put("minecraft:glow_berries", 0f);
        entries.add(new CropBiomeMatrixJsonConfig("minecraft:plains", plains));

        Map<String, Float> jungle = new LinkedHashMap<>();
        jungle.put("minecraft:wheat_seeds", 0.5f);
        jungle.put("minecraft:glow_berries", 0.5f);
        entries.add(new CropBiomeMatrixJsonConfig("minecraft:jungle", jungle));

        return entries;
    }

    private static Float asFloat(JsonElement element) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) return null;
        return element.getAsFloat();
    }
}
