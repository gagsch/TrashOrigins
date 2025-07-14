package xyz.gagsch.trashorigins;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import xyz.gagsch.trashorigins.update.ModrinthAutoUpdater;

import static xyz.gagsch.trashorigins.TrashOrigins.LOGGER;

@Mod.EventBusSubscriber(modid = TrashOrigins.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue AUTO_UPDATE = BUILDER.comment("Should Trash Origins auto-update on start-up?").define("autoUpdate", false);
    static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (AUTO_UPDATE.get() && ModrinthAutoUpdater.checkNewUpdate()) {
            ModrinthAutoUpdater.updateAndRestart();
        }

        LOGGER.debug("[TrashOrigins] Config has been registered.");
    }
}
