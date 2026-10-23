package com.holybuckets.farming.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;

import javax.annotation.Nullable;
import java.util.function.Function;

public class FreshwaterClient {

    /** Point these at hbs_farming:block/freshwater_* once custom textures exist. */
    public static final ResourceLocation STILL_TEXTURE = new ResourceLocation("minecraft", "block/water_still");
    public static final ResourceLocation FLOWING_TEXTURE = new ResourceLocation("minecraft", "block/water_flow");
    public static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation("minecraft", "block/water_overlay");

    public static final float LIGHTEN = 0.35f;
    private static final int DEFAULT_WATER_COLOR = 0x3F76E4;

    public static int getTintColor(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
        int water = (level == null || pos == null) ? DEFAULT_WATER_COLOR : BiomeColors.getAverageWaterColor(level, pos);
        return 0xFF000000 | lighten(water, LIGHTEN);
    }

    public static TextureAtlasSprite[] getSprites() {
        Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        return new TextureAtlasSprite[] { atlas.apply(STILL_TEXTURE), atlas.apply(FLOWING_TEXTURE), atlas.apply(OVERLAY_TEXTURE) };
    }

    private static int lighten(int rgb, float amount) {
        int r = lightenChannel(rgb >> 16, amount);
        int g = lightenChannel(rgb >> 8, amount);
        int b = lightenChannel(rgb, amount);
        return (r << 16) | (g << 8) | b;
    }

    private static int lightenChannel(int value, float amount) {
        int c = value & 0xFF;
        return c + Math.round((255 - c) * amount);
    }

}
