package com.holybuckets.farming;

import com.holybuckets.farming.client.CommonClassClient;
import com.holybuckets.farming.client.FreshwaterRenderHandler;
import com.holybuckets.farming.fluid.ModFluids;
import net.blay09.mods.balm.api.client.BalmClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.renderer.RenderType;


public class FreshwaterFarmingMainFabricClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        BalmClient.initialize(Constants.MOD_ID, CommonClassClient::initClient);

        FluidRenderHandlerRegistry.INSTANCE.register(ModFluids.freshwater(), ModFluids.flowingFreshwater(), new FreshwaterRenderHandler());
        BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(), ModFluids.freshwater(), ModFluids.flowingFreshwater());
    }

}
