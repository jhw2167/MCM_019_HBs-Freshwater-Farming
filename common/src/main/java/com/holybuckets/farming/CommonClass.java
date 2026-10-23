package com.holybuckets.farming;

import com.holybuckets.foundation.event.BalmEventRegister;
import com.holybuckets.farming.block.ModBlocks;
import com.holybuckets.farming.config.FreshwaterFarmingConfig;
import com.holybuckets.farming.block.be.ModBlockEntities;
import com.holybuckets.farming.fluid.ModFluids;
import com.holybuckets.farming.item.ModItems;
import com.holybuckets.farming.menu.ModMenus;
import com.holybuckets.farming.platform.Services;
import net.blay09.mods.balm.api.Balm;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;


public class CommonClass {

    public static boolean isInitialized = false;
    public static void init()
    {
        if (isInitialized)
            return;

        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", com.holybuckets.farming.platform.Services.PLATFORM.getPlatformName(), com.holybuckets.farming.platform.Services.PLATFORM.getEnvironmentName());
        Constants.LOG.info("The ID for diamonds is {}", BuiltInRegistries.ITEM.getKey(Items.DIAMOND));

        //Initialize Foundations
        com.holybuckets.foundation.FoundationInitializers.commonInitialize();

        if (Services.PLATFORM.isModLoaded(Constants.MOD_ID)) {
            Constants.LOG.info("Hello to " + Constants.MOD_NAME + "!");
        }

        //RegisterConfigs
        Balm.getConfig().registerConfig(FreshwaterFarmingConfig.class);
        FreshwaterFarmingMain.INSTANCE = new FreshwaterFarmingMain();
        BalmEventRegister.registerEvents();
        BalmEventRegister.registerCommands();
        ModFluids.initialize();
        ModBlocks.initialize(Balm.getBlocks());
        ModBlockEntities.initialize(Balm.getBlockEntities());
        ModItems.initialize(Balm.getItems());
        ModMenus.initialize(Balm.getMenus());
        
        isInitialized = true;
    }

    /**
     * Description: Run sample tests methods
     */
    public static void sample()
    {

    }
}