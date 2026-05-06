package dev.ftbcustombg.mixin;

import dev.ftbcustombg.Config;
import dev.ftbcustombg.client.CustomTextureManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

/**
 * Injects into {@code QuestScreen.drawBackground()} to replace the default
 * FTB Library 9-slice window texture with the user's custom background image.
 *
 * The target class lives in FTB Quests which is an optional dependency, so
 * {@code remap = false} and the mixin JSON uses {@code "required": false}.
 *
 * Rendering chain that gets replaced:
 *   QuestScreen.drawBackground()
 *     → BaseScreen.drawBackground() (super)
 *       → Theme.drawGui()
 *         → PartIcon.draw("ftblibrary:textures/gui/background.png")
 */
@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen", remap = false)
public abstract class QuestScreenMixin {

    /**
     * Signature mirrors {@code Panel.drawBackground(GuiGraphics, Theme, int, int, int, int)}.
     * {@code Object} is used for {@code Theme} to avoid a compile-time dependency on
     * FTB Library – Mixin accepts this because Object is the erasure of all reference types.
     */
    @Inject(
            method = "drawBackground",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void ftbcustombg_drawBackground(
            GuiGraphics graphics,
            Object theme,
            int x, int y, int w, int h,
            CallbackInfo ci
    ) {
        if (!Config.ENABLED.get()) return;

        Path imagePath = CustomTextureManager.resolveConfiguredPath();
        if (imagePath == null) return;

        ResourceLocation texId = CustomTextureManager.getOrLoad(imagePath);
        if (texId == null) return;

        int imgW = CustomTextureManager.getCachedWidth();
        int imgH = CustomTextureManager.getCachedHeight();
        if (imgW <= 0 || imgH <= 0) return;

        // Draw the custom image scaled to fill the quest panel area (x, y, w, h).
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale((float) w / imgW, (float) h / imgH, 1.0f);
        graphics.blit(texId, 0, 0, 0.0f, 0.0f, imgW, imgH, imgW, imgH);
        graphics.pose().popPose();

        // Cancel the default background rendering (prevents background.png / background_squares.png
        // from drawing on top of our custom image at the QuestScreen level).
        ci.cancel();
    }
}
