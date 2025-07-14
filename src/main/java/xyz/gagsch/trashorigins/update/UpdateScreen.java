package xyz.gagsch.trashorigins.update;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import xyz.gagsch.trashorigins.Config;

public class UpdateScreen extends Screen {
    public UpdateScreen() {
        super(Component.literal("Trash Origins Update"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Update and Restart"), button -> {
            ModrinthAutoUpdater.updateAndRestart();
        }).bounds(centerX - 100, centerY, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Later"), button -> {
            Minecraft.getInstance().setScreen(null);
        }).bounds(centerX - 100, centerY + 24, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Enable Auto-Update"), button -> {
            Config.AUTO_UPDATE.set(true);
            ModrinthAutoUpdater.updateAndRestart();
        }).bounds(centerX - 100, centerY + 48, 200, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderDirtBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}