package com.holybuckets.farming.fluid;

import com.holybuckets.farming.Constants;
import com.holybuckets.farming.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;

public class ModFluids {

    public static final String FRESHWATER = "freshwater";
    public static final String FLOWING_FRESHWATER = "flowing_freshwater";

    private static FlowingFluid freshwater;
    private static FlowingFluid flowingFreshwater;

    public static void initialize() {
        Services.PLATFORM.registerFluids();
    }

    /** Built on first use so the block and bucket can hold it before the fluid registry fires. */
    public static FlowingFluid freshwater() {
        if (freshwater == null) freshwater = Services.PLATFORM.createFreshwaterSource();
        return freshwater;
    }

    public static FlowingFluid flowingFreshwater() {
        if (flowingFreshwater == null) flowingFreshwater = Services.PLATFORM.createFreshwaterFlowing();
        return flowingFreshwater;
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
