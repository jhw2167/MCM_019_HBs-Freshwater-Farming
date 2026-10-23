package com.holybuckets.farming.platform;

import com.holybuckets.farming.fluid.FreshwaterFluid;
import com.holybuckets.farming.fluid.ModFluids;
import com.holybuckets.farming.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public FlowingFluid createFreshwaterSource() {
        return new FreshwaterFluid.Source();
    }

    @Override
    public FlowingFluid createFreshwaterFlowing() {
        return new FreshwaterFluid.Flowing();
    }

    @Override
    public LiquidBlock createFreshwaterBlock(BlockBehaviour.Properties properties) {
        return new LiquidBlock(ModFluids.freshwater(), properties);
    }

    @Override
    public BucketItem createFreshwaterBucket(Item.Properties properties) {
        return new BucketItem(ModFluids.freshwater(), properties);
    }

    @Override
    public void registerFluids() {
        Registry.register(BuiltInRegistries.FLUID, ModFluids.id(ModFluids.FRESHWATER), ModFluids.freshwater());
        Registry.register(BuiltInRegistries.FLUID, ModFluids.id(ModFluids.FLOWING_FRESHWATER), ModFluids.flowingFreshwater());
    }
}
