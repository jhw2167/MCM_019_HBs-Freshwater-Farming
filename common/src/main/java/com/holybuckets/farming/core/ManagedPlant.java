package com.holybuckets.farming.core;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nullable;

/**
 * Growth facts for one plant block, resolved once at server start so ticks never need instanceof or casts.
 */
public final class ManagedPlant {

    public enum Type { CROP, STEM, BUSH, COLUMN }

    public static final int MAX_COLUMN_HEIGHT = 3;
    public static final int MIN_LIGHT = 9;

    private final Type type;
    private final Block block;
    private final Item seed;
    @Nullable private final IntegerProperty age;
    private final int maxAge;
    @Nullable private final Block fruit;
    @Nullable private final Block attachedStem;
    private final boolean needsLight;

    private ManagedPlant(Type type, Block block, @Nullable IntegerProperty age, int maxAge, @Nullable Block fruit, @Nullable Block attachedStem) {
        this(type, block, age, maxAge, fruit, attachedStem, type != Type.COLUMN);
    }

    private ManagedPlant(Type type, Block block, @Nullable IntegerProperty age, int maxAge, @Nullable Block fruit, @Nullable Block attachedStem, boolean needsLight) {
        this.type = type;
        this.block = block;
        this.seed = block.asItem();
        this.age = age;
        this.maxAge = maxAge;
        this.fruit = fruit;
        this.attachedStem = attachedStem;
        this.needsLight = needsLight;
    }

    public Type getType() { return type; }

    public Block getBlock() { return block; }

    /** The item the crop biome matrix keys on; for crops this is the seed. */
    public Item getSeed() { return seed; }

    @Nullable
    public IntegerProperty getAge() { return age; }

    /** Fully grown age; for columns this is the full height in blocks. */
    public int getMaxAge() { return maxAge; }

    @Nullable
    public Block getFruit() { return fruit; }

    @Nullable
    public Block getAttachedStem() { return attachedStem; }

    /** Vanilla's light level 9 requirement; separate from the skylight rule. */
    public boolean needsLight() { return needsLight; }

    public boolean needsFarmland() { return type == Type.CROP || type == Type.STEM; }


    @Nullable
    public static ManagedPlant classify(Block block) {
        if (block instanceof CropBlock crop) {
            IntegerProperty age = findAge(block);
            if (age == null) return null;
            return new ManagedPlant(Type.CROP, block, age, crop.getMaxAge(), null, null);
        }
        if (block instanceof StemBlock stem) {
            StemGrownBlock fruit = stem.getFruit();
            return new ManagedPlant(Type.STEM, block, StemBlock.AGE, StemBlock.MAX_AGE, fruit, fruit.getAttachedStem());
        }
        if (block instanceof SweetBerryBushBlock) {
            return new ManagedPlant(Type.BUSH, block, SweetBerryBushBlock.AGE, SweetBerryBushBlock.MAX_AGE, null, null);
        }
        if (block instanceof NetherWartBlock) {
            return new ManagedPlant(Type.BUSH, block, NetherWartBlock.AGE, NetherWartBlock.MAX_AGE, null, null, false);
        }
        if (block instanceof SugarCaneBlock || block instanceof CactusBlock) {
            return new ManagedPlant(Type.COLUMN, block, null, MAX_COLUMN_HEIGHT, null, null);
        }
        return null;
    }

    @Nullable
    private static IntegerProperty findAge(Block block) {
        for (Property<?> property : block.getStateDefinition().getProperties()) {
            if (property instanceof IntegerProperty integer && "age".equals(property.getName())) return integer;
        }
        return null;
    }

}
