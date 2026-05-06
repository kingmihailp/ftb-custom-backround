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
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.nio.file.Path;
import java.util.List;

@EventBusSubscriber(modid = FTBCustomBG.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {}

    // ------------------------------------------------------------------
    // Resource-pack registration (MOD bus)
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        CustomTexturePackResources resources = new CustomTexturePackResources();

        event.addRepositorySource(packs -> {
            Pack pack = Pack.create(
                    FTBCustomBG.MOD_ID + ":custom_background",
                    Component.translatable("pack." + FTBCustomBG.MOD_ID + ".title"),
                    /* required = */ true,
                    /* supplier = */ id -> resources,
                    new Pack.Info(
                            Component.translatable("pack." + FTBCustomBG.MOD_ID + ".description"),
                            net.minecraft.server.packs.PackCompatibility.COMPATIBLE,
                            FeatureFlagSet.of(),
                            List.of()
                    ),
                    Pack.Position.TOP,
                    /* fixedPosition = */ true,
                    PackSource.BUILT_IN
            );
            if (pack != null) {
                packs.accept(pack);
            }
        });
    }

    // ------------------------------------------------------------------
    // Full-screen background rendering (GAME / FORGE bus)
    //
    // Registered separately in FTBCustomBG so that it rides the FORGE bus
    // (ScreenEvent lives there, not on the MOD bus).
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

        // Scale and translate so the image fills the screen exactly.
        graphics.pose().pushPose();
        graphics.pose().scale((float) screenW / imgW, (float) screenH / imgH, 1.0f);
        graphics.blit(texId, 0, 0, 0.0f, 0.0f, imgW, imgH, imgW, imgH);
        graphics.pose().popPose();
    }
}
