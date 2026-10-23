package com.holybuckets.farming.fluid;

import com.holybuckets.farming.block.ModBlocks;
import com.holybuckets.farming.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;

/**
 * Water in every respect except that it never forms new source blocks.
 */
public abstract class FreshwaterFluid extends WaterFluid {

    @Override
    public Fluid getFlowing() {
        return ModFluids.flowingFreshwater();
    }

    @Override
    public Fluid getSource() {
        return ModFluids.freshwater();
    }

    @Override
    public Item getBucket() {
        return ModItems.freshwaterBucket;
    }

    @Override
    public BlockState createLegacyBlock(FluidState state) {
        return ModBlocks.freshwater.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid instanceof FreshwaterFluid;
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    public static boolean isFreshwater(FluidState state) {
        return state.getType() instanceof FreshwaterFluid;
    }


    public static class Source extends FreshwaterFluid {

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static class Flowing extends FreshwaterFluid {

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

}
