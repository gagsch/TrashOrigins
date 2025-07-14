package xyz.gagsch.trashorigins.mixin;

import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

import static xyz.gagsch.trashorigins.power.Powers.BLESSED_LOCATION;

@Mixin(ResultSlot.class)
public class ResultSlotMixin {
    @Shadow
    @Final
    private Player player;

    @Unique
    private static void trashorigins$addEnchantments(Player player, ItemStack stack, TieredItem tieredItem) {
        RandomSource random = player.getRandom();

        IPowerContainer.get(player).ifPresent(handler -> {
            if (!handler.hasPower(BLESSED_LOCATION))
                return;

            Map<Enchantment, Integer> enchantments = new HashMap<>();
            boolean tool = !(tieredItem instanceof SwordItem);

            if (tool) {
                boolean silkTouch = random.nextBoolean();
                int weight = random.nextInt(0, 100);

                enchantments.put(Enchantments.BLOCK_EFFICIENCY, random.nextInt(0, 4));

                if (silkTouch && weight > 70) {
                    enchantments.put(Enchantments.SILK_TOUCH, 1);
                }
                else if (weight > 30) {
                    enchantments.put(Enchantments.BLOCK_FORTUNE, Math.min(3, weight / 30));
                }
            }
            else {
                boolean fireAspect = random.nextBoolean();
                boolean mobLooting = random.nextBoolean();

                if (fireAspect) {
                    enchantments.put(Enchantments.FIRE_ASPECT, random.nextInt(0, 3));
                }
                if (mobLooting) {
                    enchantments.put(Enchantments.MOB_LOOTING, random.nextInt(0, 4));
                }

                enchantments.put(Enchantments.SHARPNESS, random.nextInt(0, 6));
            }

            enchantments.put(Enchantments.UNBREAKING, random.nextInt(4, 9));

            if (random.nextInt(0, 10) == 0) {
                enchantments.put(Enchantments.MENDING, 1);
            }

            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                if (entry.getValue() > 0) {
                    stack.enchant(entry.getKey(), entry.getValue());
                }
            }
        });
    }

    @Inject(method = "remove", at = @At("TAIL"), cancellable = true)
    private void afterRemove(int amount, CallbackInfoReturnable<ItemStack> cir) {
        if (player.level().isClientSide) return;

        ItemStack stack = cir.getReturnValue();
        if (stack.getItem() instanceof TieredItem tiered && tiered.getTier() == Tiers.GOLD && !stack.isEnchanted()) {
            trashorigins$addEnchantments(player, stack, tiered);
            cir.setReturnValue(stack);
        }
    }

}
