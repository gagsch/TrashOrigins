package xyz.gagsch.trashorigins.mixin;

import io.github.edwinmindcraft.apoli.common.ApoliCommon;
import io.github.edwinmindcraft.apoli.common.action.bientity.SimpleBiEntityAction;
import io.github.edwinmindcraft.apoli.common.network.S2CPlayerMount;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleBiEntityAction.class)
public class SimpleBiEntityActionMixin {

    /**
     * @author blubby
     * @reason because its broken with entity on player
     */
    @Inject(method = "mount", at = @At("TAIL"), remap = false)
    private static void mount(Entity actor, Entity target, CallbackInfo ci) {
		if (!actor.level().isClientSide() && target instanceof ServerPlayer player) {
			ApoliCommon.CHANNEL.send(PacketDistributor.DIMENSION.with(() -> player.level().dimension()), new S2CPlayerMount(actor.getId(), target.getId()));
		}
    }
}
