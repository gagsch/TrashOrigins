package xyz.gagsch.trashorigins.power.piglin;

import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

import static xyz.gagsch.trashorigins.power.Powers.BLESSED_LOCATION;

public class GoldToolCraft {
    @SubscribeEvent
    public static void craftItem(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(event.getCrafting().getItem() instanceof TieredItem tieredItem && tieredItem.getTier() == Tiers.GOLD))
            return;

        RandomSource random = player.getRandom();

        IPowerContainer.get(player).ifPresent(handler -> {
            if (!handler.hasPower(BLESSED_LOCATION))
                return;

            Map<Enchantment, Integer> enchantments = new HashMap<>();
            boolean tool = !(tieredItem instanceof SwordItem);

            if (tool) {
                boolean silkTouch = random.nextBoolean();
                int weight = random.nextInt(0, 100);

                enchantments.put(Enchantments.BLOCK_EFFICIENCY, random.nextInt(0, 3));

                if (silkTouch && weight > 70) {
                    enchantments.put(Enchantments.SILK_TOUCH, 1);
                }
                else if (weight > 30) {
                    enchantments.put(Enchantments.BLOCK_FORTUNE, weight / 33);
                }
            }
            else {
                boolean fireAspect = random.nextBoolean();
                boolean mobLooting = random.nextBoolean();

                if (fireAspect) {
                    enchantments.put(Enchantments.FIRE_ASPECT, random.nextInt(0, 2));
                }
                if (mobLooting) {
                    enchantments.put(Enchantments.MOB_LOOTING, random.nextInt(0, 3));
                }

                enchantments.put(Enchantments.SHARPNESS, random.nextInt(0, 5));
            }

            enchantments.put(Enchantments.UNBREAKING, random.nextInt(7, 10));

            if (random.nextInt(0, 10) == 10) {
                enchantments.put(Enchantments.MENDING, 1);
            }

            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                if (entry.getValue() > 0) {
                    event.getCrafting().enchant(entry.getKey(), entry.getValue());
                }
            }
        });
    }
}
