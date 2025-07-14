package xyz.gagsch.trashorigins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import xyz.gagsch.trashorigins.update.ModrinthAutoUpdater;
import xyz.gagsch.trashorigins.update.UpdateScreen;
import xyz.gagsch.trashorigins.power.Powers;
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
        MinecraftForge.EVENT_BUS.register(ClientModEvents.class);
        MinecraftForge.EVENT_BUS.register(PiglinBehavior.class);

        Powers.POWER_FACTORIES.register(bus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static class ClientModEvents {
        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void onScreenInit(ScreenEvent.Init.Post event) {
            if (!(event.getScreen() instanceof TitleScreen) || !ModrinthAutoUpdater.checkNewUpdate()) return;

            int width = event.getScreen().width;
            int height = event.getScreen().height;

            Button button = Button.builder(Component.literal("Trash Origins Update"), btn -> {
                Minecraft.getInstance().setScreen(new UpdateScreen());
            }).bounds(width / 2 - 100, height / 4 + 156, 200, 20).build();

            event.addListener(button);
        }
    }

    public static class FogStuff {
        @OnlyIn(Dist.CLIENT)
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
