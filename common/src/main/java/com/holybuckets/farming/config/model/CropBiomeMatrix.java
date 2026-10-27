package com.holybuckets.farming.config.model;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;

//All stats for Crop growth against every biome
public class CropBiomeMatrix {

    public static final float DEFAULT_MODIFIER = 1f;
    public static final CropBiomeMatrix EMPTY = new CropBiomeMatrix(Map.of());

    private final Map<Holder<Biome>, Map<Item, Float>> modifiers;

    public CropBiomeMatrix(Map<Holder<Biome>, Map<Item, Float>> modifiers)
    {
        Map<Holder<Biome>, Map<Item, Float>> sealed = new HashMap<>();
        modifiers.forEach((biome, seeds) -> sealed.put(biome, Map.copyOf(seeds)));
        this.modifiers = Map.copyOf(sealed);
    }

    public float getModifier(Holder<Biome> biome, Item seed) {
        Map<Item, Float> seeds = modifiers.get(biome);
        if (seeds == null) return DEFAULT_MODIFIER;
        return seeds.getOrDefault(seed, DEFAULT_MODIFIER);
    }

    public boolean canGrow(Holder<Biome> biome, Item seed) {
        return getModifier(biome, seed) > 0f;
    }

    public Map<Holder<Biome>, Map<Item, Float>> getModifiers() {
        return modifiers;
    }

    public Map<Item, Float> getModifiers(Holder<Biome> biome) {
        return modifiers.getOrDefault(biome, Map.of());
    }


    public int size() {
        return modifiers.size();
    }

}
