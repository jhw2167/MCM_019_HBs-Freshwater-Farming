package com.holybuckets.farming;

import com.holybuckets.farming.client.CommonClassClient;
import com.holybuckets.farming.client.IBewlrRenderer;
import com.holybuckets.farming.fluid.ModFluids;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
public class FreshwaterFarmingMainForgeClient {


    public static void clientInitializeForge() {
        CommonClassClient.initClient();
        //Item challengeChest = ModBlocks.challengeChest.asItem();
       // setBlockEntityRender( challengeChest, ChallengeItemBlockRenderer.CHEST_RENDERER);
    }

    public static void initModBus(IEventBus modEventBus) {
        modEventBus.addListener(FreshwaterFarmingMainForgeClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModFluids.freshwater(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.flowingFreshwater(), RenderType.translucent());
        });
    }

        private static void setBlockEntityRender(Object item, BlockEntityWithoutLevelRenderer renderer) {
            ((IBewlrRenderer) item).setBlockEntityWithoutLevelRenderer(renderer);
        }

}
