package xyz.gagsch.trashorigins.powers.piglin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import xyz.gagsch.trashorigins.TrashOrigins;

import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("all")
public interface IPiglinPower {
    ResourceLocation PIGLIN_NEUTRAL = ResourceLocation.fromNamespaceAndPath(TrashOrigins.MODID, "piglin/piglin_neutral");
    ResourceLocation PIGLIN_CAPITALISM = ResourceLocation.fromNamespaceAndPath(TrashOrigins.MODID, "piglin/capitalism");
}
