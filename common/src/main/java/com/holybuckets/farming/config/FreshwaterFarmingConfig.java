package com.holybuckets.farming.config;

import com.holybuckets.farming.Constants;
import com.holybuckets.farming.config.json.CropBiomeMatrixJsonConfig;
import net.blay09.mods.balm.api.config.reflection.Comment;
import net.blay09.mods.balm.api.config.reflection.Config;
import net.blay09.mods.balm.api.config.reflection.NestedType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Config(Constants.MOD_ID)
public class FreshwaterFarmingConfig {

    @Comment("Standard expected number of minutes for a crop to grow from seeds to fully mature. This expected value is modified by the JSON config which will speed or slow this expectation. A day in Minecraft is 20 minutes.")
    public int standardGrowthTimeMins = 120;

    @Comment("Where the crop biome matrix json can be found. This file sets a growth speed modifier for each seed in each biome")
    public String cropBiomeMatrixConfig = CropBiomeMatrixJsonConfig.DEF_CONFIG_FILE_PATH;

    @Comment("Block names the game also treats as freshwater. Any of these within the vanilla range hydrates farmland just like water, e.g. \"minecraft:water_cauldron\"")
    @NestedType(String.class)
    public List<String> alternateFreshWaterTypes = new ArrayList<>();

    @Comment("Biomes recognized as sources of freshwater even if the freshwater block does not spawn here. This makes crops grow near normal water sources in biomes like rivers, giving you a nice early game farming alternative. Biome tags are accepted with a leading #")
    @NestedType(String.class)
    public List<String> alternateRiverBiomes = new ArrayList<>(List.of("#minecraft:is_river"));

    @Comment("Seed items whose plants grow without access to the sky, like nether wart. Every other managed plant needs open sky above it to grow")
    @NestedType(String.class)
    public Set<String> ignoreNeedsSkylight = new HashSet<>(Set.of("minecraft:nether_wart"));

}
