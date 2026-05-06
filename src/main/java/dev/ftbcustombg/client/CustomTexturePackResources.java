package dev.ftbcustombg.client;

import dev.ftbcustombg.Config;
import dev.ftbcustombg.FTBCustomBG;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A virtual resource pack that replaces configured FTB Library / FTB Quests
 * background textures with the user's custom image file.
 */
public class CustomTexturePackResources implements PackResources {

    private static final String PACK_ID = FTBCustomBG.MOD_ID + ":custom_background";

    // Minimal pack.mcmeta – pack_format 34 covers Minecraft 1.21.x
    private static final byte[] PACK_META = """
            {"pack":{"description":"FTB Custom Background","pack_format":34}}
            """.stripIndent().getBytes(StandardCharsets.UTF_8);

    // -----------------------------------------------------------------------

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... paths) {
        if (paths.length == 1 && "pack.mcmeta".equals(paths[0])) {
            return () -> new ByteArrayInputStream(PACK_META);
        }
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.CLIENT_RESOURCES) return null;
        if (!Config.ENABLED.get()) return null;
        if (!isTargeted(location)) return null;

        Path image = CustomTextureManager.resolveConfiguredPath();
        if (image == null || !Files.exists(image)) return null;

        return () -> {
            try {
                return Files.newInputStream(image);
            } catch (IOException e) {
                FTBCustomBG.LOGGER.error("Could not open custom background file '{}'", image, e);
                throw e;
            }
        };
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES) return;
        if (!Config.ENABLED.get()) return;

        Path image = CustomTextureManager.resolveConfiguredPath();
        if (image == null || !Files.exists(image)) return;

        for (ResourceLocation target : getTargetLocations()) {
            if (target.getNamespace().equals(namespace) && target.getPath().startsWith(path)) {
                output.accept(target, () -> Files.newInputStream(image));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        if (type != PackType.CLIENT_RESOURCES) return Set.of();
        if (!Config.ENABLED.get()) return Set.of();
        return getTargetLocations().stream()
                .map(ResourceLocation::getNamespace)
                .collect(Collectors.toSet());
    }

    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionSerializer<T> deserializer) {
        return null;
    }

    @Override
    public String packId() {
        return PACK_ID;
    }

    @Override
    public boolean isBuiltin() {
        return true;
    }

    @Override
    public void close() {}

    // -----------------------------------------------------------------------

    private boolean isTargeted(ResourceLocation loc) {
        return getTargetLocations().stream().anyMatch(t -> t.equals(loc));
    }

    private List<ResourceLocation> getTargetLocations() {
        return Config.TARGET_TEXTURES.get().stream()
                .map(s -> ResourceLocation.tryParse((String) s))
                .filter(Objects::nonNull)
                .toList();
    }
}
