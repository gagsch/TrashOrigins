package xyz.gagsch.trashorigins.mixin;

import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static xyz.gagsch.trashorigins.power.Powers.BLIND_LOCATION;

@Mixin(LightTexture.class)
public class LightTextureMixin {
    @Inject(method = "calculateDarknessScale", at = @At("HEAD"), cancellable = true)
    private void setupColor(LivingEntity entity, float darkness, float gamma, CallbackInfoReturnable<Float> cir) {
        IPowerContainer.get(entity).ifPresent(handler -> {
            if (handler.hasPower(BLIND_LOCATION)) {
                cir.setReturnValue(0f);
            }
        });
    }
}
