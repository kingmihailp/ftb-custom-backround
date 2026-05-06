package dev.ftbcustombg;

import com.mojang.logging.LogUtils;
import dev.ftbcustombg.client.ClientEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(FTBCustomBG.MOD_ID)
public class FTBCustomBG {

    public static final String MOD_ID = "ftbcustombg";
    public static final Logger LOGGER = LogUtils.getLogger();

    // ModContainer is injected by NeoForge – replaces ModLoadingContext.get().registerConfig()
    public FTBCustomBG(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        if (dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.addListener(ClientEvents::onScreenRenderPre);
        }
    }

    public static void invalidateTextureCache() {
        dev.ftbcustombg.client.CustomTextureManager.invalidate();
    }
}
