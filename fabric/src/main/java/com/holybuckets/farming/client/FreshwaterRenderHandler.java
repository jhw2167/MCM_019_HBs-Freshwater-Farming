package com.holybuckets.farming.client;

import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class FreshwaterRenderHandler extends SimpleFluidRenderHandler {

    public FreshwaterRenderHandler() {
        super(FreshwaterClient.STILL_TEXTURE, FreshwaterClient.FLOWING_TEXTURE, FreshwaterClient.OVERLAY_TEXTURE);
    }

    @Override
    public int getFluidColor(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
        return FreshwaterClient.getTintColor(view, pos);
    }

}
