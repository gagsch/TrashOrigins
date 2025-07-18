package xyz.gagsch.trashorigins.mixin;

import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static xyz.gagsch.trashorigins.power.Powers.PIGLIN_NEUTRAL_LOCATION;

@Mixin(PiglinAi.class)
public class PiglinAiMixin {
    @Unique
    private static boolean trashorigins$isThePlayerNearAnyOwnedPiglins(Entity entity) {
        List<Piglin> nearbyOwnedPiglins = entity.level().getEntitiesOfClass(Piglin.class, entity.getBoundingBox().inflate(16), piglin -> piglin.getPersistentData().contains("owner"));
        return !nearbyOwnedPiglins.isEmpty();
    }

    @Inject(method = "angerNearbyPiglins", at = @At("HEAD"), cancellable = true)
    private static void angerNearbyPiglins(Player player, boolean ignoreVision, CallbackInfo ci) {
        IPowerContainer.get(player).ifPresent(handler -> {
            if (handler.hasPower(PIGLIN_NEUTRAL_LOCATION)) {
                ci.cancel();
            }
        });

        if (trashorigins$isThePlayerNearAnyOwnedPiglins(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "isWearingGold", at = @At("RETURN"), cancellable = true)
    private static void isWearingGold(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        IPowerContainer.get(entity).ifPresent(handler -> {
            cir.setReturnValue(handler.hasPower(PIGLIN_NEUTRAL_LOCATION));
        });

        if (trashorigins$isThePlayerNearAnyOwnedPiglins(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "wantsToStopFleeing", at = @At("HEAD"), cancellable = true)
    private static void wantsToStopFleeing(Piglin piglin, CallbackInfoReturnable<Boolean> cir) {
        if (piglin.getPersistentData().contains("owner")) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isNearAvoidTarget", at = @At("HEAD"), cancellable = true)
    private static void isNearAvoidTarget(Piglin piglin, CallbackInfoReturnable<Boolean> cir) {
        if (piglin.getPersistentData().contains("owner")) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isNearZombified", at = @At("HEAD"), cancellable = true)
    private static void isNearZombified(Piglin piglin, CallbackInfoReturnable<Boolean> cir) {
        if (piglin.getPersistentData().contains("owner")) {
            cir.setReturnValue(false);
        }
    }
}
