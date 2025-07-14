package xyz.gagsch.trashorigins.mixin;

import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Monster.class)
public class MonsterMixin {
    @Inject(method = "isPreventingPlayerRest", at = @At("TAIL"), cancellable = true)
    public void isPreventingPlayerRest(Player player, CallbackInfoReturnable<Boolean> cir) {
        Monster monster = (Monster) (Object) this;
        if (monster.getPersistentData().contains("owner")) {
            cir.setReturnValue(false);
        }
    }
}
