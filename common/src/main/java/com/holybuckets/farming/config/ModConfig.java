package com.holybuckets.farming.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.holybuckets.farming.LoggerProject;
import com.holybuckets.farming.config.json.CropBiomeMatrixJsonConfig;
import com.holybuckets.farming.config.model.CropBiomeMatrix;
import com.holybuckets.farming.fluid.FreshwaterFluid;
import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.modelInterface.IStringSerializable;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ModConfig {

    private static final String CLASS_ID = "002";
    private static ModConfig INSTANCE;

    private CropBiomeMatrix cropBiomeMatrix = CropBiomeMatrix.EMPTY;
    private Set<Block> alternateFreshwaterBlocks = Set.of();
    private Set<Holder<Biome>> alternateRiverBiomes = Set.of();
    private Set<Item> ignoreNeedsSkylight = Set.of();
    private int stemPlantResetAge = DEFAULT_STEM_RESET_AGE;

    public static final int DEFAULT_STEM_RESET_AGE = 4;
    public static final int MAX_STEM_AGE = 7;

    public static ModConfig getInstance() {
        if (INSTANCE == null) INSTANCE = new ModConfig();
        return INSTANCE;
    }

    private ModConfig() {}

    public static void clear() {
        INSTANCE = null;
    }


    public CropBiomeMatrix getCropBiomeMatrix() {
        return cropBiomeMatrix;
    }

    public Map<Item, Float> getGrowthModifier(Holder<Biome> biome) {
        return cropBiomeMatrix.getModifiers(biome);
    }

    public float getGrowthModifier(Holder<Biome> biome, Item seed) {
        return cropBiomeMatrix.getModifier(biome, seed);
    }


    public boolean hasAlternateFreshwater() {
        return !alternateFreshwaterBlocks.isEmpty();
    }

    public boolean isAlternateFreshwater(Block block) {
        return alternateFreshwaterBlocks.contains(block);
    }

    /** Age a stem drops back to when its melon or pumpkin is removed. */
    public int getStemPlantResetAge() {
        return stemPlantResetAge;
    }

    /** Seeds listed in ignoreNeedsSkylight grow without open sky. */
    public boolean ignoresSkylight(Item seed) {
        return ignoreNeedsSkylight.contains(seed);
    }

    public boolean hasRiverBiomes() {
        return !alternateRiverBiomes.isEmpty();
    }

    /** Whether ordinary water in this biome counts as freshwater; pass the holder from level.getBiome(pos). */
    public boolean isRiverBiome(Holder<Biome> biome) {
        return alternateRiverBiomes.contains(biome);
    }

    /** Freshwater fluid or any block listed in alternateFreshWaterTypes. */
    public boolean isFreshwaterSource(BlockState state) {
        return alternateFreshwaterBlocks.contains(state.getBlock()) || FreshwaterFluid.isFreshwater(state.getFluidState());
    }


    /** Called by FreshwaterFarmingMain before the server starts, once the general config is active. */
    public void load(RegistryAccess registries, FreshwaterFarmingConfig generalConfig) {
        alternateFreshwaterBlocks = resolveBlocks(generalConfig.alternateFreshWaterTypes);
        alternateRiverBiomes = resolveBiomes(generalConfig.alternateRiverBiomes, registries);
        ignoreNeedsSkylight = resolveItems(generalConfig.ignoreNeedsSkylight);
        stemPlantResetAge = Math.max(0, Math.min(MAX_STEM_AGE, generalConfig.stemPlantResetAge));

        File configFile = new File(generalConfig.cropBiomeMatrixConfig);
        File defaultConfigFile = new File(CropBiomeMatrixJsonConfig.DEF_CONFIG_FILE_PATH);

        String json = HBUtil.FileIO.loadJsonConfigs(configFile, defaultConfigFile, defaultJsonConfig());

        List<CropBiomeMatrixJsonConfig> entries;
        try {
            entries = CropBiomeMatrixJsonConfig.parse(JsonParser.parseString(json).getAsJsonObject());
        } catch (RuntimeException e) {
            String msg = String.format(
                "Failed to parse crop biome matrix JSON: %s. Error:\n %s.\n\nDefault configs will be applied",
                configFile.getAbsolutePath(), e.getMessage());
            LoggerProject.logError(CLASS_ID + "002", msg);
            entries = CropBiomeMatrixJsonConfig.buildDefaultConfig();
        }

        cropBiomeMatrix = resolve(entries, registries);
        LoggerProject.logInfo(CLASS_ID + "001", "Loaded crop growth modifiers for " + cropBiomeMatrix.size() + " biome(s)");
    }

    private Set<Item> resolveItems(Collection<String> itemNames) {
        Set<Item> items = new ReferenceOpenHashSet<>();
        if (itemNames == null) return items;

        for (String name : itemNames) {
            ResourceLocation id = ResourceLocation.tryParse(name);
            Optional<Item> item = id == null ? Optional.empty() : BuiltInRegistries.ITEM.getOptional(id);
            if (item.isEmpty() || item.get() == Items.AIR) {
                LoggerProject.logWarning(CLASS_ID + "010", "Unknown item '" + name + "' in ignoreNeedsSkylight; skipping");
                continue;
            }
            items.add(item.get());
        }

        LoggerProject.logInfo(CLASS_ID + "011", items.size() + " seed type(s) grow without skylight");
        return items;
    }

    private Set<Block> resolveBlocks(List<String> blockNames) {
        Set<Block> blocks = new ReferenceOpenHashSet<>();
        if (blockNames == null) return blocks;

        for (String name : blockNames) {
            ResourceLocation id = ResourceLocation.tryParse(name);
            Optional<Block> block = id == null ? Optional.empty() : BuiltInRegistries.BLOCK.getOptional(id);
            if (block.isEmpty() || block.get() == Blocks.AIR) {
                LoggerProject.logWarning(CLASS_ID + "006", "Unknown block '" + name + "' in alternateFreshWaterTypes; skipping");
                continue;
            }
            blocks.add(block.get());
        }

        LoggerProject.logInfo(CLASS_ID + "007", "Treating " + blocks.size() + " alternate block type(s) as freshwater");
        return blocks;
    }

    /** Biome ids and #tags become the server's live holders, so lookups match level.getBiome(pos) by identity. */
    private Set<Holder<Biome>> resolveBiomes(List<String> biomeNames, RegistryAccess registries) {
        Set<Holder<Biome>> biomes = new ReferenceOpenHashSet<>();
        if (biomeNames == null) return biomes;
        Registry<Biome> biomeRegistry = registries.registryOrThrow(Registries.BIOME);

        for (String name : biomeNames) {
            boolean isTag = name.startsWith("#");
            ResourceLocation id = ResourceLocation.tryParse(isTag ? name.substring(1) : name);
            if (id == null) {
                LoggerProject.logWarning(CLASS_ID + "008", "Invalid biome '" + name + "' in alternateRiverBiomes; skipping");
                continue;
            }

            if (isTag) {
                Optional<? extends Iterable<Holder<Biome>>> tag = biomeRegistry.getTag(TagKey.create(Registries.BIOME, id));
                if (tag.isEmpty()) {
                    LoggerProject.logWarning(CLASS_ID + "008", "Unknown biome tag '" + name + "' in alternateRiverBiomes; skipping");
                    continue;
                }
                tag.get().forEach(biomes::add);
            } else {
                Optional<Holder.Reference<Biome>> biome = biomeRegistry.getHolder(ResourceKey.create(Registries.BIOME, id));
                if (biome.isEmpty()) {
                    LoggerProject.logWarning(CLASS_ID + "008", "Unknown biome '" + name + "' in alternateRiverBiomes; skipping");
                    continue;
                }
                biomes.add(biome.get());
            }
        }

        LoggerProject.logInfo(CLASS_ID + "009", "Treating water in " + biomes.size() + " biome(s) as freshwater");
        return biomes;
    }

    /** Turns configured biome and seed names into registry objects; unknown names are skipped with a warning. */
    private CropBiomeMatrix resolve(List<CropBiomeMatrixJsonConfig> configs, RegistryAccess registries) {
        Registry<Biome> biomeRegistry = registries.registryOrThrow(Registries.BIOME);
        Map<Holder<Biome>, Map<Item, Float>> resolved = new HashMap<>();
        int skipped = 0;

        for (CropBiomeMatrixJsonConfig config : configs) {
            Optional<Holder.Reference<Biome>> biome = Optional.ofNullable(ResourceLocation.tryParse(config.getBiomeId()))
                .flatMap(id -> biomeRegistry.getHolder(ResourceKey.create(Registries.BIOME, id)));
            if (biome.isEmpty()) {
                LoggerProject.logWarning(CLASS_ID + "003", "Unknown biome '" + config.getBiomeId() + "' in crop biome matrix; skipping");
                skipped++;
                continue;
            }

            Map<Item, Float> seeds = new HashMap<>();
            for (Map.Entry<String, Float> entry : config.getModifiers().entrySet()) {
                ResourceLocation itemId = ResourceLocation.tryParse(entry.getKey());
                Optional<Item> seed = itemId == null ? Optional.empty() : BuiltInRegistries.ITEM.getOptional(itemId);
                if (seed.isEmpty() || entry.getValue() < 0f) {
                    LoggerProject.logWarning(CLASS_ID + "004", "Invalid seed entry '" + entry.getKey() + "': " + entry.getValue()
                        + " under biome '" + config.getBiomeId() + "'; skipping");
                    skipped++;
                    continue;
                }
                seeds.put(seed.get(), entry.getValue());
            }
            resolved.put(biome.get(), seeds);
        }

        LoggerProject.logInfo(CLASS_ID + "005", "Resolved crop biome matrix, skipped " + skipped + " invalid entrie(s)");
        return new CropBiomeMatrix(resolved);
    }

    private static IStringSerializable defaultJsonConfig() {
        return new IStringSerializable() {
            @Override
            public String serialize() {
                return new GsonBuilder().setPrettyPrinting().create()
                    .toJson(CropBiomeMatrixJsonConfig.toJsonObject(CropBiomeMatrixJsonConfig.buildDefaultConfig()));
            }

            @Override
            public void deserialize(String jsonString) {
                // No-op for default config
            }
        };
    }

}
