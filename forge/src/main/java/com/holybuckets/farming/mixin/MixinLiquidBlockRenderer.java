package com.holybuckets.farming.mixin;

import com.holybuckets.farming.client.FreshwaterClient;
import com.holybuckets.farming.fluid.FreshwaterFluid;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LiquidBlockRenderer.class)
public class MixinLiquidBlockRenderer {

    @Redirect(method = "tesselate", at = @At(value = "INVOKE", remap = false,
        target = "Lnet/minecraftforge/client/ForgeHooksClient;getFluidSprites(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;)[Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private TextureAtlasSprite[] hbs_farming$getFluidSprites(BlockAndTintGetter level, BlockPos pos, FluidState state) {
        if (FreshwaterFluid.isFreshwater(state)) return FreshwaterClient.getSprites();
        return ForgeHooksClient.getFluidSprites(level, pos, state);
    }

    @Redirect(method = "tesselate", at = @At(value = "INVOKE", remap = false,
        target = "Lnet/minecraftforge/client/extensions/common/IClientFluidTypeExtensions;getTintColor(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I"))
    private int hbs_farming$getTintColor(IClientFluidTypeExtensions extensions, FluidState state, BlockAndTintGetter level, BlockPos pos) {
        if (FreshwaterFluid.isFreshwater(state)) return FreshwaterClient.getTintColor(level, pos);
        return extensions.getTintColor(state, level, pos);
    }

}
