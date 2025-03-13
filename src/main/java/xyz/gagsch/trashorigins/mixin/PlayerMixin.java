package xyz.gagsch.trashorigins.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
    /**
     * @author blubby
     * @reason broken when riding player
     */
    @Overwrite
    public double getMyRidingOffset() {
        return ((Player)((Object) this)).getVehicle() instanceof Player ? 0 : -0.35;
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void attack(Entity entity, CallbackInfo ci) {
        Player self = ((Player)((Object) this));
        if (entity.getVehicle() == self) {
            ci.cancel();
        }
    }
}
