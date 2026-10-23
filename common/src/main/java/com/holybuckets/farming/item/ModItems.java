package com.holybuckets.farming.item;


import com.holybuckets.farming.Constants;
import com.holybuckets.farming.block.ModBlocks;
import com.holybuckets.farming.platform.Services;
import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.item.BalmItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModItems {
    public static DeferredObject<CreativeModeTab> creativeModeTab;
    public static Item emptyBlockItem;
    public static Item freshwaterBucket;

    public static void initialize(BalmItems items) {
        //items.registerItem(() -> emptyBlockItem = new EmptyBlockItem(items.itemProperties()), id("empty_block"));
        items.registerItem(() -> freshwaterBucket = Services.PLATFORM.createFreshwaterBucket(bucketProperties()), id("freshwater_bucket"));
        creativeModeTab = items.registerCreativeModeTab(id(Constants.MOD_ID), () -> new ItemStack(ModBlocks.templateBlock));
    }

    private static Item.Properties bucketProperties() {
        return new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1);
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
