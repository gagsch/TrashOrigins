package xyz.gagsch.trashorigins;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import xyz.gagsch.trashorigins.powers.Powers;
import xyz.gagsch.trashorigins.powers.piglin.GoldToolCraft;
import xyz.gagsch.trashorigins.powers.piglin.PiglinBehavior;

@Mod(TrashOrigins.MODID)
public class TrashOrigins {
    public static final String MODID = "trashorigins";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TrashOrigins() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(PiglinBehavior.class);
        MinecraftForge.EVENT_BUS.register(GoldToolCraft.class);

        Powers.POWER_FACTORIES.register(bus);
    }
}
