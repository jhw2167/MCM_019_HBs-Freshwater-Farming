package com.holybuckets.farming.fluid;

import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;

/**
 * Shares water's FluidType because Forge routes swimming, drowning and isInWater through it.
 */
public class ForgeFreshwaterFluid {

    public static class Source extends FreshwaterFluid.Source {
        @Override
        public FluidType getFluidType() {
            return ForgeMod.WATER_TYPE.get();
        }
    }

    public static class Flowing extends FreshwaterFluid.Flowing {
        @Override
        public FluidType getFluidType() {
            return ForgeMod.WATER_TYPE.get();
        }
    }

}
