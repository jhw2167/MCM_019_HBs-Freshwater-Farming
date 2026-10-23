package com.holybuckets.farming.platform;

import com.holybuckets.farming.Constants;
import com.holybuckets.farming.fluid.ForgeFreshwaterFluid;
import com.holybuckets.farming.fluid.ModFluids;
import com.holybuckets.farming.platform.services.IPlatformHelper;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgePlatformHelper implements IPlatformHelper {

    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, Constants.MOD_ID);

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public FlowingFluid createFreshwaterSource() {
        return new ForgeFreshwaterFluid.Source();
    }

    @Override
    public FlowingFluid createFreshwaterFlowing() {
        return new ForgeFreshwaterFluid.Flowing();
    }

    @Override
    public LiquidBlock createFreshwaterBlock(BlockBehaviour.Properties properties) {
        return new LiquidBlock(ModFluids::freshwater, properties);
    }

    @Override
    public BucketItem createFreshwaterBucket(Item.Properties properties) {
        return new BucketItem(ModFluids::freshwater, properties);
    }

    @Override
    public void registerFluids() {
        FLUIDS.register(ModFluids.FRESHWATER, ModFluids::freshwater);
        FLUIDS.register(ModFluids.FLOWING_FRESHWATER, ModFluids::flowingFreshwater);
        FLUIDS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
