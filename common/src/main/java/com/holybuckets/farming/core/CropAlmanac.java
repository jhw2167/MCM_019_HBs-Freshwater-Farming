package com.holybuckets.farming.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.holybuckets.farming.config.ModConfig;
import com.holybuckets.farming.config.model.CropBiomeMatrix;
import com.holybuckets.foundation.networking.SimpleStringMessage;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Builds the Farmer's Almanac on the server: one page per managed seed with a fresh random pick of biomes each time it opens.
 */
public class CropAlmanac {

    public static final String MSG_PAGE = "hbs_farming.almanac_page";
    public static final String MSG_OPEN = "hbs_farming.almanac_open";

    public static final String KEY_SEED = "seed";
    public static final String KEY_FAVORABLE = "favorable";
    public static final String KEY_POOR = "poor";

    public static final int BIOMES_PER_LIST = 5;

    /** Sends one small message per page so the book never exceeds the message size limit, then tells the client to open it. */
    public static void open(ServerPlayer player) {
        CropBiomeMatrix matrix = ModConfig.getInstance().getCropBiomeMatrix();
        Registry<Biome> biomeRegistry = player.serverLevel().registryAccess().registryOrThrow(Registries.BIOME);
        List<Holder.Reference<Biome>> biomes = biomeRegistry.holders().toList();

        List<Item> seeds = new ArrayList<>(CropGrowthManager.getManagedSeeds());
        seeds.sort(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));

        for (Item seed : seeds) {
            SimpleStringMessage.createAndFire(player, MSG_PAGE, buildPage(seed, biomes, matrix).toString());
        }
        SimpleStringMessage.createAndFire(player, MSG_OPEN, String.valueOf(seeds.size()));
    }

    private static JsonObject buildPage(Item seed, List<Holder.Reference<Biome>> biomes, CropBiomeMatrix matrix) {
        List<ResourceLocation> favorable = new ArrayList<>();
        List<ResourceLocation> poor = new ArrayList<>();

        for (Holder.Reference<Biome> biome : biomes) {
            ResourceLocation id = biome.key().location();
            if (matrix.getModifier(biome, seed) >= 1f) favorable.add(id);
            else poor.add(id);
        }

        JsonObject page = new JsonObject();
        page.addProperty(KEY_SEED, BuiltInRegistries.ITEM.getKey(seed).toString());
        page.add(KEY_FAVORABLE, randomPick(favorable));
        page.add(KEY_POOR, randomPick(poor));
        return page;
    }

    private static JsonArray randomPick(List<ResourceLocation> biomes) {
        Collections.shuffle(biomes);
        JsonArray picked = new JsonArray();
        for (int i = 0; i < Math.min(BIOMES_PER_LIST, biomes.size()); i++) {
            picked.add(biomes.get(i).toString());
        }
        return picked;
    }

}
