package com.holybuckets.farming.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.holybuckets.farming.LoggerProject;
import com.holybuckets.farming.core.CropAlmanac;
import com.holybuckets.foundation.client.ClientEventRegistrar;
import com.holybuckets.foundation.event.custom.SimpleMessageEvent;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects almanac pages sent by the server and opens them in the vanilla book screen.
 */
public class AlmanacClient {

    private static final String CLASS_ID = "004";
    private static final List<Component> PENDING_PAGES = new ArrayList<>();

    public static void init(ClientEventRegistrar registrar) {
        registrar.registerOnSimpleMessage(CropAlmanac.MSG_PAGE, AlmanacClient::onPageMessage);
        registrar.registerOnSimpleMessage(CropAlmanac.MSG_OPEN, AlmanacClient::onOpenMessage);
    }

    private static void onPageMessage(SimpleMessageEvent event) {
        try {
            JsonObject page = JsonParser.parseString(event.getContent()).getAsJsonObject();
            Component built = buildPage(page);
            Minecraft.getInstance().execute(() -> PENDING_PAGES.add(built));
        } catch (RuntimeException e) {
            LoggerProject.logError(CLASS_ID + "001", "Could not read almanac page: " + event.getContent());
        }
    }

    private static void onOpenMessage(SimpleMessageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            List<Component> pages = List.copyOf(PENDING_PAGES);
            PENDING_PAGES.clear();
            if (pages.isEmpty()) return;
            minecraft.setScreen(new BookViewScreen(new BookViewScreen.BookAccess() {
                @Override
                public int getPageCount() {
                    return pages.size();
                }

                @Override
                public FormattedText getPageRaw(int index) {
                    return pages.get(index);
                }
            }));
        });
    }

    private static Component buildPage(JsonObject page) {
        ResourceLocation seedId = new ResourceLocation(page.get(CropAlmanac.KEY_SEED).getAsString());
        Component seedName = BuiltInRegistries.ITEM.get(seedId).getDescription();

        return Component.translatable("book.hbs_farming.almanac.crop", seedName)
            .append("\n\n")
            .append(Component.translatable("book.hbs_farming.almanac.favorable", biomeList(page.getAsJsonArray(CropAlmanac.KEY_FAVORABLE))))
            .append("\n\n")
            .append(Component.translatable("book.hbs_farming.almanac.poor", biomeList(page.getAsJsonArray(CropAlmanac.KEY_POOR))));
    }

    private static Component biomeList(JsonArray biomes) {
        if (biomes == null || biomes.isEmpty()) return Component.translatable("book.hbs_farming.almanac.none");

        MutableComponent list = Component.empty();
        for (int i = 0; i < biomes.size(); i++) {
            if (i > 0) list.append(", ");
            JsonElement biome = biomes.get(i);
            list.append(Component.translatable(Util.makeDescriptionId("biome", new ResourceLocation(biome.getAsString()))));
        }
        return list;
    }

}
