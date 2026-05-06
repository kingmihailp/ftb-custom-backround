package dev.ftbcustombg;

import com.mojang.logging.LogUtils;
import dev.ftbcustombg.client.ClientEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(FTBCustomBG.MOD_ID)
public class FTBCustomBG {

    public static final String MOD_ID = "ftbcustombg";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FTBCustomBG(IEventBus modEventBus, Dist dist) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        if (dist == Dist.CLIENT) {
            // ScreenEvent.Render.Pre lives on the NeoForge / game event bus
            NeoForge.EVENT_BUS.addListener(ClientEvents::onScreenRenderPre);
        }
    }
}
