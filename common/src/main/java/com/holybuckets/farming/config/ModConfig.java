package com.holybuckets.farming.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.holybuckets.farming.LoggerProject;
import com.holybuckets.farming.config.json.CropBiomeMatrixJsonConfig;
import com.holybuckets.farming.config.model.CropBiomeMatrix;
import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.foundation.modelInterface.IStringSerializable;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.EventPriority;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.blay09.mods.balm.api.event.server.ServerStoppedEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModConfig {

    private static final String CLASS_ID = "002";
    private static ModConfig INSTANCE;

    private CropBiomeMatrix cropBiomeMatrix = CropBiomeMatrix.EMPTY;

    public static ModConfig getInstance() {
        if (INSTANCE == null) INSTANCE = new ModConfig();
        return INSTANCE;
    }

    private ModConfig() {}

    public static void init(EventRegistrar registrar) {
        INSTANCE = ModConfig.getInstance();
        registrar.registerOnBeforeServerStarted(ModConfig::onBeforeServerStarted, EventPriority.High);
        registrar.registerOnServerStopped(ModConfig::onServerStopped, EventPriority.Low);
    }


    public CropBiomeMatrix getCropBiomeMatrix() {
        return cropBiomeMatrix;
    }

    public float getGrowthModifier(Holder<Biome> biome, Item seed) {
        return cropBiomeMatrix.getModifier(biome, seed);
    }


    private void onBeforeServerStarted(RegistryAccess registries) {
        FreshwaterFarmingConfig activeConfig = Balm.getConfig().getActiveConfig(FreshwaterFarmingConfig.class);
        File configFile = new File(activeConfig.cropBiomeMatrixConfig);
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
            entries = CropBiomeMatrixJsonConfig.defaultConfig();
        }

        cropBiomeMatrix = resolve(entries, registries);
        LoggerProject.logInfo(CLASS_ID + "001", "Loaded crop growth modifiers for " + cropBiomeMatrix.size() + " biome(s)");
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

    private void onServerStopped() {
        INSTANCE = null;
    }


    private static void onBeforeServerStarted(ServerStartingEvent event) {
        getInstance().onBeforeServerStarted(event.getServer().registryAccess());
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        getInstance().onServerStopped();
    }


    private static IStringSerializable defaultJsonConfig() {
        return new IStringSerializable() {
            @Override
            public String serialize() {
                return new GsonBuilder().setPrettyPrinting().create()
                    .toJson(CropBiomeMatrixJsonConfig.toJsonObject(CropBiomeMatrixJsonConfig.defaultConfig()));
            }

            @Override
            public void deserialize(String jsonString) {
                // No-op for default config
            }
        };
    }

}
