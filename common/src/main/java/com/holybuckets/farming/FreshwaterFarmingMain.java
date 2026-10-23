package com.holybuckets.farming;


import com.holybuckets.foundation.event.EventRegistrar;
import com.holybuckets.farming.config.FreshwaterFarmingConfig;
import com.holybuckets.farming.config.ModConfig;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.EventPriority;
import net.blay09.mods.balm.api.event.server.ServerStartingEvent;

/**
 * Main instance of the mod, initialize this class statically via commonClass
 * This class will init all major Manager instances and events for the mod
 */
public class FreshwaterFarmingMain {
    private static boolean DEV_MODE = false;;
    private static FreshwaterFarmingConfig CONFIG;
    public static FreshwaterFarmingMain INSTANCE;

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
        //ChallengeBlockBehavior.init(registrar);
        ModConfig.init(registrar);


        //register local events
        registrar.registerOnBeforeServerStarted(this::onServerStarting);

    }

    private void onServerStarting(ServerStartingEvent e) {
        CONFIG = Balm.getConfig().getActiveConfig(FreshwaterFarmingConfig.class);
        //this.DEV_MODE = CONFIG.devMode;
        this.DEV_MODE = false;
    }


}
