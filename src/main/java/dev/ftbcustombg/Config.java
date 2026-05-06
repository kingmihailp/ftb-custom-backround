package dev.ftbcustombg;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.ConfigValue<String> BACKGROUND_PATH;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> TARGET_TEXTURES;
    public static final ModConfigSpec.BooleanValue FULLSCREEN;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("FTB Custom Background configuration");

        ENABLED = BUILDER
                .comment("Enable / disable the custom background.")
                .define("enabled", true);

        BACKGROUND_PATH = BUILDER
                .comment(
                        "Path to your custom background image (PNG).",
                        "Relative path is resolved from the Minecraft game directory.",
                        "Example: config/ftbcustombg/background.png"
                )
                .define("background_path", "config/ftbcustombg/background.png");

        TARGET_TEXTURES = BUILDER
                .comment(
                        "FTB Library / FTB Quests resource-pack texture paths that will be",
                        "replaced with your custom image.",
                        "  background.png        – the 9-slice window frame drawn around the quest GUI",
                        "  background_squares.png – the tiled dot pattern in the quest-map area",
                        "After changing this value reload resources with F3+T."
                )
                .defineList(
                        "target_textures",
                        List.of(
                                "ftblibrary:textures/gui/background.png",
                                "ftblibrary:textures/gui/background_squares.png"
                        ),
                        obj -> obj instanceof String s && s.contains(":")
                );

        FULLSCREEN = BUILDER
                .comment(
                        "When true, the custom image is also drawn as a full-screen background",
                        "behind the quest panel (replaces the dark Minecraft screen overlay).",
                        "Combine with target_textures overrides for the best result."
                )
                .define("fullscreen", false);

        SPEC = BUILDER.build();
    }
}
