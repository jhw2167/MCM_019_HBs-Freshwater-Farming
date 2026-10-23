package com.holybuckets.farming;

import com.holybuckets.farming.client.CommonClassClient;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod( Constants.MOD_ID)
public class FreshwaterFarmingMainForge {

    public FreshwaterFarmingMainForge() {
        super();
        Balm.initialize(Constants.MOD_ID, CommonClass::init);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> BalmClient.initialize(Constants.MOD_ID, CommonClassClient::initClient));
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> FreshwaterFarmingMainForgeClient.initModBus(FMLJavaModLoadingContext.get().getModEventBus()));
    }


}
