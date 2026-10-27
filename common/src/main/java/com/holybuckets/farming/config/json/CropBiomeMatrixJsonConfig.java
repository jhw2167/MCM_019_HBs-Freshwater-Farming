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
    public static final String COMMENT = "Define each object with a biome name. Inside it add the seeds of a crop and a numeric growth speed modifier."
        + " 0 disables the crop in that biome, 2 grows twice as fast. Unlisted biome and seed pairings default to 1";

    /** Column order of the default matrix: the item each managed vanilla plant is keyed on. */
    public static final String[] DEFAULT_CROPS = {
        "minecraft:wheat_seeds",
        "minecraft:carrot",
        "minecraft:potato",
        "minecraft:beetroot_seeds",
        "minecraft:melon_seeds",
        "minecraft:pumpkin_seeds",
        "minecraft:sweet_berries",
        "minecraft:sugar_cane",
        "minecraft:cactus",
        "minecraft:torchflower_seeds"
    };

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

    public Map<String, Float> getModifiers() {
        return Collections.unmodifiableMap(modifiers);
    }

    public JsonObject serialize() {
        JsonObject obj = new JsonObject();
        modifiers.forEach(obj::addProperty);
        return obj;
    }

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

    public static List<CropBiomeMatrixJsonConfig> buildDefaultConfig() {
        List<CropBiomeMatrixJsonConfig> entries = new ArrayList<>();

        entries.add(row("minecraft:plains",                   1.5f,   1.2f,   1.2f,   1.2f,   1.0f,   1.2f,   0.75f,  1.0f,   0.25f,  1.0f));
        entries.add(row("minecraft:sunflower_plains",         1.5f,   1.2f,   1.2f,   1.2f,   1.2f,   1.2f,   0.75f,  1.0f,   0.25f,  1.25f));
        entries.add(row("minecraft:meadow",                   1.25f,  1.2f,   1.5f,   1.2f,   0.5f,   1.0f,   1.0f,   0.5f,   0.0f,   1.5f));
        entries.add(row("minecraft:cherry_grove",             1.0f,   1.0f,   1.2f,   1.0f,   0.75f,  1.0f,   1.2f,   0.75f,  0.0f,   2.0f));

        entries.add(row("minecraft:forest",                   0.75f,  1.0f,   1.0f,   1.0f,   0.75f,  1.2f,   1.5f,   0.75f,  0.0f,   1.0f));
        entries.add(row("minecraft:flower_forest",            0.75f,  1.0f,   1.0f,   1.0f,   0.75f,  1.2f,   1.5f,   0.75f,  0.0f,   2.0f));
        entries.add(row("minecraft:birch_forest",             0.75f,  1.0f,   1.0f,   1.0f,   0.75f,  1.2f,   1.25f,  0.75f,  0.0f,   1.0f));
        entries.add(row("minecraft:old_growth_birch_forest",  0.75f,  1.0f,   1.0f,   1.0f,   0.75f,  1.2f,   1.25f,  0.75f,  0.0f,   1.0f));
        entries.add(row("minecraft:dark_forest",              0.5f,   0.75f,  0.75f,  0.75f,  0.5f,   1.0f,   1.25f,  0.5f,   0.0f,   0.5f));

        entries.add(row("minecraft:taiga",                    0.5f,   1.0f,   1.2f,   1.2f,   0.25f,  1.0f,   2.0f,   0.5f,   0.0f,   0.75f));
        entries.add(row("minecraft:old_growth_pine_taiga",    0.5f,   1.0f,   1.2f,   1.2f,   0.25f,  1.0f,   2.0f,   0.5f,   0.0f,   0.75f));
        entries.add(row("minecraft:old_growth_spruce_taiga",  0.5f,   1.0f,   1.2f,   1.2f,   0.25f,  1.0f,   2.0f,   0.5f,   0.0f,   0.75f));

        entries.add(row("minecraft:snowy_plains",             0.25f,  0.5f,   0.5f,   0.5f,   0.0f,   0.25f,  0.75f,  0.0f,   0.0f,   0.25f));
        entries.add(row("minecraft:snowy_taiga",              0.25f,  0.5f,   0.5f,   0.5f,   0.0f,   0.25f,  1.0f,   0.0f,   0.0f,   0.25f));
        entries.add(row("minecraft:grove",                    0.25f,  0.25f,  0.5f,   0.25f,  0.0f,   0.25f,  1.0f,   0.0f,   0.0f,   0.25f));
        entries.add(row("minecraft:snowy_slopes",             0.0f,   0.0f,   0.25f,  0.0f,   0.0f,   0.0f,   0.25f,  0.0f,   0.0f,   0.0f));
        entries.add(row("minecraft:ice_spikes",               0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f));
        entries.add(row("minecraft:frozen_peaks",             0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f));
        entries.add(row("minecraft:jagged_peaks",             0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f));
        entries.add(row("minecraft:snowy_beach",              0.0f,   0.25f,  0.25f,  0.25f,  0.0f,   0.0f,   0.25f,  0.0f,   0.0f,   0.0f));
        entries.add(row("minecraft:frozen_river",             0.25f,  0.25f,  0.5f,   0.25f,  0.0f,   0.25f,  0.5f,   0.0f,   0.0f,   0.25f));

        entries.add(row("minecraft:windswept_hills",          0.5f,   0.75f,  1.2f,   1.0f,   0.25f,  0.75f,  1.0f,   0.5f,   0.25f,  0.75f));
        entries.add(row("minecraft:windswept_gravelly_hills", 0.25f,  0.5f,   1.0f,   0.75f,  0.25f,  0.5f,   0.75f,  0.25f,  0.25f,  0.5f));
        entries.add(row("minecraft:windswept_forest",         0.5f,   0.75f,  1.2f,   1.0f,   0.25f,  0.75f,  1.25f,  0.5f,   0.0f,   0.75f));
        entries.add(row("minecraft:stony_peaks",              0.25f,  0.25f,  0.75f,  0.5f,   0.0f,   0.25f,  0.5f,   0.25f,  0.25f,  0.5f));

        entries.add(row("minecraft:savanna",                  1.0f,   0.75f,  0.75f,  0.75f,  1.5f,   0.75f,  0.5f,   0.75f,  1.0f,   1.0f));
        entries.add(row("minecraft:savanna_plateau",          1.0f,   0.75f,  0.75f,  0.75f,  1.5f,   0.75f,  0.5f,   0.5f,   1.0f,   1.0f));
        entries.add(row("minecraft:windswept_savanna",        0.75f,  0.5f,   0.75f,  0.5f,   1.25f,  0.5f,   0.5f,   0.5f,   1.0f,   0.75f));

        entries.add(row("minecraft:desert",                   0.25f,  0.25f,  0.25f,  0.25f,  0.5f,   0.0f,   0.0f,   0.5f,   2.0f,   0.25f));
        entries.add(row("minecraft:badlands",                 0.25f,  0.25f,  0.5f,   0.25f,  0.75f,  0.25f,  0.0f,   0.25f,  1.5f,   0.25f));
        entries.add(row("minecraft:eroded_badlands",          0.25f,  0.25f,  0.5f,   0.25f,  0.75f,  0.25f,  0.0f,   0.25f,  1.5f,   0.25f));
        entries.add(row("minecraft:wooded_badlands",          0.5f,   0.5f,   0.75f,  0.5f,   1.0f,   0.5f,   0.25f,  0.25f,  1.25f,  0.5f));

        entries.add(row("minecraft:jungle",                   0.5f,   0.75f,  0.5f,   0.5f,   2.0f,   0.75f,  1.0f,   2.0f,   0.0f,   1.5f));
        entries.add(row("minecraft:sparse_jungle",            0.75f,  0.75f,  0.75f,  0.75f,  2.0f,   1.0f,   1.0f,   1.5f,   0.0f,   1.5f));
        entries.add(row("minecraft:bamboo_jungle",            0.5f,   0.75f,  0.5f,   0.5f,   1.5f,   0.75f,  1.0f,   2.0f,   0.0f,   1.25f));

        entries.add(row("minecraft:swamp",                    0.5f,   0.75f,  0.75f,  0.75f,  0.75f,  1.0f,   0.75f,  2.0f,   0.0f,   0.75f));
        entries.add(row("minecraft:mangrove_swamp",           0.25f,  0.5f,   0.5f,   0.5f,   1.0f,   0.75f,  0.5f,   2.0f,   0.0f,   0.75f));

        entries.add(row("minecraft:river",                    2.0f,   1.5f,   1.5f,   1.5f,   1.5f,   1.5f,   1.0f,   2.0f,   0.25f,  1.25f));

        entries.add(row("minecraft:beach",                    0.25f,  0.5f,   0.5f,   1.0f,   0.75f,  0.5f,   0.25f,  1.5f,   1.0f,   0.5f));
        entries.add(row("minecraft:stony_shore",              0.25f,  0.25f,  0.5f,   0.75f,  0.25f,  0.25f,  0.25f,  0.5f,   0.25f,  0.25f));
        entries.add(row("minecraft:mushroom_fields",          0.25f,  0.5f,   0.75f,  0.5f,   0.25f,  0.75f,  0.25f,  0.5f,   0.0f,   0.25f));

        entries.add(row("minecraft:warm_ocean",               0.5f,   0.5f,   0.5f,   0.75f,  1.0f,   0.5f,   0.25f,  1.5f,   0.75f,  0.75f));
        entries.add(row("minecraft:lukewarm_ocean",           0.5f,   0.75f,  0.75f,  0.75f,  0.75f,  0.75f,  0.5f,   1.25f,  0.5f,   0.75f));
        entries.add(row("minecraft:deep_lukewarm_ocean",      0.5f,   0.75f,  0.75f,  0.75f,  0.75f,  0.75f,  0.5f,   1.25f,  0.5f,   0.75f));
        entries.add(row("minecraft:ocean",                    0.5f,   0.75f,  0.75f,  0.75f,  0.5f,   0.75f,  0.5f,   1.0f,   0.25f,  0.5f));
        entries.add(row("minecraft:deep_ocean",               0.5f,   0.75f,  0.75f,  0.75f,  0.5f,   0.75f,  0.5f,   1.0f,   0.25f,  0.5f));
        entries.add(row("minecraft:cold_ocean",               0.25f,  0.5f,   0.75f,  0.5f,   0.25f,  0.5f,   0.75f,  0.5f,   0.0f,   0.25f));
        entries.add(row("minecraft:deep_cold_ocean",          0.25f,  0.5f,   0.75f,  0.5f,   0.25f,  0.5f,   0.75f,  0.5f,   0.0f,   0.25f));
        entries.add(row("minecraft:frozen_ocean",             0.0f,   0.0f,   0.25f,  0.0f,   0.0f,   0.0f,   0.25f,  0.0f,   0.0f,   0.0f));
        entries.add(row("minecraft:deep_frozen_ocean",        0.0f,   0.0f,   0.25f,  0.0f,   0.0f,   0.0f,   0.25f,  0.0f,   0.0f,   0.0f));

        entries.add(row("minecraft:lush_caves",               1.0f,   1.2f,   1.2f,   1.2f,   1.2f,   1.0f,   1.5f,   1.2f,   0.0f,   1.5f));
        entries.add(row("minecraft:dripstone_caves",          0.5f,   0.5f,   0.75f,  0.5f,   0.25f,  0.5f,   0.25f,  0.5f,   0.5f,   0.25f));
        entries.add(row("minecraft:deep_dark",                0.25f,  0.25f,  0.25f,  0.25f,  0.0f,   0.25f,  0.0f,   0.0f,   0.0f,   0.0f));

        entries.add(row("minecraft:nether_wastes",            0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f));
        entries.add(row("minecraft:crimson_forest",           0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f));
        entries.add(row("minecraft:warped_forest",            0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f));
        entries.add(row("minecraft:soul_sand_valley",         0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f));
        entries.add(row("minecraft:basalt_deltas",            0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f));

        entries.add(row("minecraft:the_end",                  0.25f,  0.25f,  0.25f,  0.25f,  0.25f,  0.25f,  0.0f,   0.0f,   0.25f,  0.25f));
        entries.add(row("minecraft:end_highlands",            0.25f,  0.25f,  0.25f,  0.25f,  0.25f,  0.25f,  0.0f,   0.0f,   0.25f,  0.25f));
        entries.add(row("minecraft:end_midlands",             0.25f,  0.25f,  0.25f,  0.25f,  0.25f,  0.25f,  0.0f,   0.0f,   0.25f,  0.25f));
        entries.add(row("minecraft:end_barrens",              0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f));
        entries.add(row("minecraft:small_end_islands",        0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f,   0.0f));

        return entries;
    }

    private static CropBiomeMatrixJsonConfig row(String biomeId, float... speeds) {
        if (speeds.length != DEFAULT_CROPS.length) {
            throw new IllegalArgumentException("Default row for " + biomeId + " has " + speeds.length + " values, expected " + DEFAULT_CROPS.length);
        }
        Map<String, Float> modifiers = new LinkedHashMap<>();
        for (int i = 0; i < speeds.length; i++) {
            modifiers.put(DEFAULT_CROPS[i], speeds[i]);
        }
        return new CropBiomeMatrixJsonConfig(biomeId, modifiers);
    }

    private static Float asFloat(JsonElement element) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) return null;
        return element.getAsFloat();
    }
}
