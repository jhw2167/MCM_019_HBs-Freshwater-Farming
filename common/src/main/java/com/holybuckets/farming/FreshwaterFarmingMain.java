package com.holybuckets.farming;


import com.holybuckets.farming.config.FreshwaterFarmingConfig;
import com.holybuckets.farming.config.ModConfig;
import com.holybuckets.farming.core.CropGrowthManager;
import com.holybuckets.foundation.event.EventRegistrar;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.EventPriority;
import net.blay09.mods.balm.api.event.LevelLoadingEvent;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;
import net.blay09.mods.balm.api.event.server.ServerStoppedEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;

/**
 * Main instance of the mod, initialize this class statically via commonClass
 * This class will init all major Manager instances and events for the mod
 */
public class FreshwaterFarmingMain {
    private static boolean DEV_MODE = false;
    public static FreshwaterFarmingMain INSTANCE;

    private FreshwaterFarmingConfig generalConfig;
    private ModConfig modConfig;

    public FreshwaterFarmingMain()
    {
        super();
        INSTANCE = this;
        init();
        // LoggerProject.logInit( "001000", this.getClass().getName() ); // Uncomment if you have a logging system in place
    }

    private void init()
    {

        /*
        Proxy for external APIs which are platform dependent
        this.portalApi = (PortalApi) Balm.platformProxy()
            .withFabric("com.holybuckets.challengetemple.externalapi.FabricPortalApi")
            .withForge("com.holybuckets.challengetemple.externalapi.ForgePortalApi")
            .build();
            */

        //Events
        EventRegistrar registrar = EventRegistrar.getInstance();
        CropGrowthManager.init(registrar);

        //register local events
        registrar.registerOnBeforeServerStarted(this::onBeforeServerStarted, EventPriority.High);
        registrar.registerOnLevelLoad(this::onLevelLoad, EventPriority.Normal);
        registrar.registerOnLevelUnload(this::onLevelUnload, EventPriority.Low);
        registrar.registerOnServerStopped(this::onServerStopped, EventPriority.Low);

    }

    /** Order matters: general config, then json configs, then managers that read them; levels load after this. */
    private void onBeforeServerStarted(ServerStartingEvent e) {
        MinecraftServer server = e.getServer();

        generalConfig = Balm.getConfig().getActiveConfig(FreshwaterFarmingConfig.class);
        DEV_MODE = false;

        modConfig = ModConfig.getInstance();
        modConfig.load(server.registryAccess(), generalConfig);

        CropGrowthManager.onServerStarting(server, generalConfig, modConfig);
    }

    private void onLevelLoad(LevelLoadingEvent.Load e) {
        if (e.getLevel() instanceof ServerLevel level) {
            CropGrowthManager.onLevelLoad(level);
        }
    }

    private void onLevelUnload(LevelLoadingEvent.Unload e) {
        CropGrowthManager.onLevelUnload(e.getLevel());
    }

    private void onServerStopped(ServerStoppedEvent e) {
        CropGrowthManager.onServerStopped();
        modConfig = null;
        generalConfig = null;
        ModConfig.clear();
    }


    public FreshwaterFarmingConfig getGeneralConfig() {
        return generalConfig;
    }

    public ModConfig getModConfig() {
        return modConfig;
    }

    @Nullable
    public CropGrowthManager getCropGrowthManager(LevelAccessor level) {
        return CropGrowthManager.get(level);
    }

}
