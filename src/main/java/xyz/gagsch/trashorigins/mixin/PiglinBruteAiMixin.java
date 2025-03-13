package xyz.gagsch.trashorigins.mixin;

import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinBruteAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.gagsch.trashorigins.powers.piglin.IPiglinPower;

import java.util.Optional;

@Mixin(PiglinBruteAi.class)
public class PiglinBruteAiMixin implements IPiglinPower {
    /**
     * @author blubby
     * @reason piglins go after all players no matter what, changed to not go after piglin neutral players
     */
    @Inject(method = "findNearestValidAttackTarget", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private static void findNearestValidAttackTarget(AbstractPiglin piglin, CallbackInfoReturnable<Optional<? extends LivingEntity>> cir) {
        Optional<? extends LivingEntity> optionalLivingEntity = cir.getReturnValue();

        if (optionalLivingEntity.isPresent()) {
            IPowerContainer.get(optionalLivingEntity.get()).ifPresent(handler -> {
                if (handler.hasPower(PIGLIN_NEUTRAL)) {
                    cir.setReturnValue(Optional.empty());
                }
            });
        }
    }
}
