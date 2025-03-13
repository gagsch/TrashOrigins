package xyz.gagsch.trashorigins.mixin;

import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.gagsch.trashorigins.powers.piglin.IPiglinPower;

@Mixin(PiglinAi.class)
public class PiglinAiMixin implements IPiglinPower {
    @Inject(method = "angerNearbyPiglins", at = @At("HEAD"), cancellable = true)
    private static void angerNearbyPiglins(Player player, boolean ignoreVision, CallbackInfo ci) {
        IPowerContainer.get(player).ifPresent(handler -> {
            if (handler.hasPower(PIGLIN_NEUTRAL)) {
                ci.cancel();
            }
        });
    }

    @Inject(method = "isWearingGold", at = @At("RETURN"), cancellable = true)
    private static void isWearingGold(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        IPowerContainer.get(entity).ifPresent(handler -> {
            cir.setReturnValue(handler.hasPower(PIGLIN_NEUTRAL));
        });
    }
}
