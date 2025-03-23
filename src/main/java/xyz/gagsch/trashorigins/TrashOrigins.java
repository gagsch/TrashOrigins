package xyz.gagsch.trashorigins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import xyz.gagsch.trashorigins.power.Powers;
import xyz.gagsch.trashorigins.power.piglin.GoldToolCraft;
import xyz.gagsch.trashorigins.power.piglin.PiglinBehavior;
import org.slf4j.Logger;

import java.util.OptionalInt;

import static xyz.gagsch.trashorigins.power.Powers.*;

@Mod(TrashOrigins.MODID)
public class TrashOrigins {
    public static final String MODID = "trashorigins";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TrashOrigins() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(FogStuff.class);
        MinecraftForge.EVENT_BUS.register(PiglinBehavior.class);
        MinecraftForge.EVENT_BUS.register(GoldToolCraft.class);

        Powers.POWER_FACTORIES.register(bus);
    }

    public static class FogStuff {
        @SubscribeEvent
        public static void renderFog(ViewportEvent.RenderFog event) {
            if (event.getType() == FogType.NONE) {
                IPowerContainer.get(event.getCamera().getEntity()).ifPresent(handler -> {
                    if (handler.hasPower(BLIND_LOCATION) && handler.hasPower(ECHO_LOCATE_LOCATION)) {
                        OptionalInt cooldown = handler.getPower(ECHO_LOCATE_LOCATION).value().getValue(handler.getOwner());

                        float start = getStart(cooldown);

                        RenderSystem.setShaderFogStart(start);
                        RenderSystem.setShaderFogEnd(start + 3);
                    }
                });
            }
        }

        private static float getStart(OptionalInt cooldown) {
            float start = 7.0f;

            if (cooldown.isPresent() && cooldown.getAsInt() > 1100) {
                int value = 1200 - cooldown.getAsInt();

                if (value < 13) {
                    start += value;
                }
                else if (value > 87) {
                    start -= value - 100;
                }
                else {
                    start = 20;
                }
            }

            return start;
        }
    }
}
