package xyz.gagsch.trashorigins.mixin;

import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBruteAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static xyz.gagsch.trashorigins.power.Powers.PIGLIN_NEUTRAL_LOCATION;

@Mixin(PiglinBruteAi.class)
public class PiglinBruteAiMixin {
    /**
     * @author blubby
     * @reason piglins go after all players no matter what, changed to not go after piglin neutral players
     */
    @Inject(method = "findNearestValidAttackTarget", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private static void findNearestValidAttackTarget(AbstractPiglin piglin, CallbackInfoReturnable<Optional<? extends LivingEntity>> cir) {
        Optional<? extends LivingEntity> optionalLivingEntity = cir.getReturnValue();

        if (optionalLivingEntity.isPresent()) {
            IPowerContainer.get(optionalLivingEntity.get()).ifPresent(handler -> {
                if (handler.hasPower(PIGLIN_NEUTRAL_LOCATION)) {
                    cir.setReturnValue(Optional.empty());
                }
            });
        }
    }
}
