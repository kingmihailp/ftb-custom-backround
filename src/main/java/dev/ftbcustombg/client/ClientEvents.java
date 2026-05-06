package dev.ftbcustombg.client;

import dev.ftbcustombg.Config;
import dev.ftbcustombg.FTBCustomBG;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.nio.file.Path;
import java.util.Optional;

@EventBusSubscriber(modid = FTBCustomBG.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {}

    // ------------------------------------------------------------------
    // Resource-pack registration (MOD bus)
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        // PackLocationInfo replaces the old (id, title, required, PackSource) parameters
        // from MC 1.20.x. Pack.readMetaAndCreate reads pack.mcmeta from the resources
        // object, so no Pack.Info / PackCompatibility construction is needed.
        PackLocationInfo info = new PackLocationInfo(
                FTBCustomBG.MOD_ID + ":custom_background",
                Component.translatable("pack." + FTBCustomBG.MOD_ID + ".title"),
                PackSource.BUILT_IN,
                Optional.empty()
        );

        // PackSelectionConfig replaced the bare Pack.Position parameter in NeoForge 1.21.1.
        // (required=true, defaultPosition=TOP, fixedPosition=true)
        PackSelectionConfig selectionConfig = new PackSelectionConfig(true, Pack.Position.TOP, true);

        event.addRepositorySource(consumer -> {
            Pack pack = Pack.readMetaAndCreate(
                    info,
                    pli -> new CustomTexturePackResources(pli),
                    PackType.CLIENT_RESOURCES,
                    selectionConfig
            );
            if (pack != null) {
                consumer.accept(pack);
            }
        });
    }

    // ------------------------------------------------------------------
    // Full-screen background rendering (FORGE / GAME bus)
    // Registered in FTBCustomBG via NeoForge.EVENT_BUS.addListener().
    // ------------------------------------------------------------------

    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (!Config.ENABLED.get() || !Config.FULLSCREEN.get()) return;

        Screen screen = event.getScreen();
        // Only activate for FTB Quests screens
        if (!screen.getClass().getName().startsWith("dev.ftb.mods.ftbquests")) return;

        Path imagePath = CustomTextureManager.resolveConfiguredPath();
        if (imagePath == null) return;

        ResourceLocation texId = CustomTextureManager.getOrLoad(imagePath);
        if (texId == null) return;

        int imgW = CustomTextureManager.getCachedWidth();
        int imgH = CustomTextureManager.getCachedHeight();
        if (imgW <= 0 || imgH <= 0) return;

        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        GuiGraphics graphics = event.getGuiGraphics();
        graphics.pose().pushPose();
        graphics.pose().scale((float) screenW / imgW, (float) screenH / imgH, 1.0f);
        graphics.blit(texId, 0, 0, 0.0f, 0.0f, imgW, imgH, imgW, imgH);
        graphics.pose().popPose();
    }
}
