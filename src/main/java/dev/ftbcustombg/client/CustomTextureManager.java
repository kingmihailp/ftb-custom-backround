package dev.ftbcustombg.client;

import dev.ftbcustombg.FTBCustomBG;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.mojang.blaze3d.platform.NativeImage;
import net.neoforged.fml.loading.FMLPaths;

public final class CustomTextureManager {

    private static final ResourceLocation TEXTURE_ID =
            ResourceLocation.fromNamespaceAndPath(FTBCustomBG.MOD_ID, "dynamic/custom_bg");

    private static @Nullable Path lastLoadedPath = null;
    private static int cachedWidth = 0;
    private static int cachedHeight = 0;
    private static boolean registered = false;

    private CustomTextureManager() {}

    /**
     * Returns the cached ResourceLocation for the custom background texture,
     * loading it from disk if necessary.  Must be called from the render thread.
     */
    public static @Nullable ResourceLocation getOrLoad(Path imagePath) {
        if (imagePath == null || !Files.exists(imagePath)) {
            return null;
        }
        if (imagePath.equals(lastLoadedPath) && registered) {
            return TEXTURE_ID;
        }
        try (InputStream stream = Files.newInputStream(imagePath)) {
            NativeImage image = NativeImage.read(stream);
            cachedWidth = image.getWidth();
            cachedHeight = image.getHeight();
            DynamicTexture texture = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(TEXTURE_ID, texture);
            lastLoadedPath = imagePath;
            registered = true;
            return TEXTURE_ID;
        } catch (IOException e) {
            FTBCustomBG.LOGGER.error("Failed to load custom background from '{}'", imagePath, e);
            return null;
        }
    }

    public static int getCachedWidth() {
        return cachedWidth;
    }

    public static int getCachedHeight() {
        return cachedHeight;
    }

    /** Resolves the configured path string to an absolute Path. */
    public static @Nullable Path resolveConfiguredPath() {
        String raw = dev.ftbcustombg.Config.BACKGROUND_PATH.get();
        if (raw == null || raw.isBlank()) return null;
        Path p = Paths.get(raw);
        if (!p.isAbsolute()) {
            p = FMLPaths.GAMEDIR.get().resolve(p);
        }
        return p;
    }

    /** Drop cached state so the texture is reloaded on next render. */
    public static void invalidate() {
        registered = false;
        lastLoadedPath = null;
    }
}
