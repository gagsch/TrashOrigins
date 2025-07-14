package xyz.gagsch.trashorigins.power.piglin;

import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static xyz.gagsch.trashorigins.power.Powers.PIGLIN_CAPITALISM_LOCATION;

public class PiglinBehavior {
    public static final Map<Player, List<AbstractPiglin>> PIGLIN_BEHAVIOR_MAP = new HashMap<>();
    public static final PiglinTeleporter PIGLIN_TELEPORTER = new PiglinTeleporter();

    public static boolean addBehavior(Player player, AbstractPiglin piglin, ItemStack itemstack) {
        if (piglin.isBaby() || PIGLIN_BEHAVIOR_MAP.containsKey(player) && (PIGLIN_BEHAVIOR_MAP.get(player).size() >= 15 || PIGLIN_BEHAVIOR_MAP.get(player).contains(piglin))) {
            return false;
        }

        boolean value = false;

        if (itemstack.is(Items.GOLD_INGOT) && !piglin.getPersistentData().hasUUID("owner")) {
            piglin.getPersistentData().putUUID("owner", player.getUUID());
            itemstack.shrink(1);
            value = true;
        }

        if (value || (piglin.getPersistentData().hasUUID("owner") && player.getUUID().equals(piglin.getPersistentData().getUUID("owner")))) {
            PIGLIN_BEHAVIOR_MAP.computeIfAbsent(player, k -> new ArrayList<>()).add(piglin);
            piglin.setImmuneToZombification(true);
            value = true;
        }

        return value;
    }

    @SubscribeEvent
    public static void onPiglinInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (event.getTarget() instanceof AbstractPiglin piglin && !event.getLevel().isClientSide()) {
            IPowerContainer.get(player).ifPresent(handler -> {
                if (!handler.hasPower(PIGLIN_CAPITALISM_LOCATION))
                    return;

                ItemStack itemstack = player.getItemInHand(event.getHand());

                SimpleParticleType packet = null;

                if (itemstack.is(ItemTags.PIGLIN_REPELLENTS) && PIGLIN_BEHAVIOR_MAP.containsKey(player) && PIGLIN_BEHAVIOR_MAP.get(player).contains(piglin)) {
                    packet = ParticleTypes.ANGRY_VILLAGER;

                    itemstack.hurtAndBreak(1, player, p -> {});
                    piglin.getPersistentData().remove("owner");
                    PIGLIN_BEHAVIOR_MAP.get(player).remove(piglin);
                }
                else if (addBehavior(player, piglin, itemstack)) {
                    packet = ParticleTypes.HAPPY_VILLAGER;
                }

                if (packet != null) {
                    ((ServerPlayer) player).connection.send(new ClientboundLevelParticlesPacket(
                            packet, false,
                            piglin.getX(), piglin.getY() + 1, piglin.getZ(),
                            0.5f, 0.5f, 0.5f,
                            0.1f, 15
                    ));
                    event.setCancellationResult(InteractionResult.CONSUME);
                    event.setCanceled(true);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        Iterator<Player> playerIterator = PIGLIN_BEHAVIOR_MAP.keySet().iterator();

        while (playerIterator.hasNext()) {
            Player player = playerIterator.next();

            if (player.isDeadOrDying()) {
                for (AbstractPiglin piglin : PIGLIN_BEHAVIOR_MAP.get(player)) {
                    piglin.getPersistentData().remove("owner");
                    piglin.setImmuneToZombification(false);
                }
                PIGLIN_BEHAVIOR_MAP.get(player).clear();
                playerIterator.remove();
                continue;
            }

            Iterator<AbstractPiglin> iterator = PIGLIN_BEHAVIOR_MAP.get(player).iterator();

            LivingEntity target = getTarget(player);

            boolean targetNull = target == null || target.isRemoved() || player.distanceToSqr(target) > 400;

            Optional<UUID> currentTarget = Optional.empty();
            UUID targetUUID = null;

            while (iterator.hasNext()) {
                AbstractPiglin abstractPiglin = iterator.next();
                Brain<?> brain = abstractPiglin.getBrain();

                if (!abstractPiglin.isAlive()) {
                    if (abstractPiglin.isDeadOrDying()) {
                        player.sendSystemMessage(abstractPiglin.getCombatTracker().getDeathMessage());
                    }
                    iterator.remove();
                    continue;
                }

                if (!targetNull && !(target instanceof AbstractPiglin)) {
                    targetUUID = target.getUUID();
                    currentTarget = brain.getMemory(MemoryModuleType.ANGRY_AT).isPresent() && brain.getMemory(MemoryModuleType.ANGRY_AT).get().equals(abstractPiglin.getPersistentData().getUUID("owner")) ? currentTarget : brain.getMemory(MemoryModuleType.ANGRY_AT);
                }

                double distance = player.distanceToSqr(abstractPiglin);

                if (distance > 400) {
                    brain.eraseMemory(MemoryModuleType.ANGRY_AT);
                    brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(player.blockPosition(), 1f, 1));
                    if (distance > 1600) {
                        abstractPiglin.teleportTo(player.getX(), player.getY(), player.getZ());
                    }
                }
                else if (!targetNull && currentTarget.isPresent() && currentTarget.get() != targetUUID) {
                    brain.setMemory(MemoryModuleType.ANGRY_AT, targetUUID);
                } else if (!targetNull && currentTarget.isEmpty()) {
                    brain.setMemory(MemoryModuleType.ANGRY_AT, targetUUID);
                } else if (targetNull) {
                    brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(player.blockPosition(), 0.95f, 4));
                }
            }
        }
    }

    private static @Nullable LivingEntity getTarget(Player player) {
        var lastHurt = player.getLastHurtMob();
        var lastHurtBy = player.getLastHurtByMob();

        if (lastHurt instanceof Player && lastHurt == lastHurtBy) {
            return lastHurt;
        }

        return findNearestHostile(player);
    }

    private static @Nullable LivingEntity findNearestHostile(Player player) {
        Level level = player.level();
        AABB area = player.getBoundingBox().inflate(10);
        List<Mob> entities = level.getNearbyEntities(Mob.class, TargetingConditions.forCombat(), player, area);

        return entities.stream()
                .filter(e -> !(e instanceof AbstractPiglin) && e.isAlive() && player == e.getTarget())
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .orElse(null);
    }

    @SubscribeEvent
    public static void dimensionTravel(EntityTravelToDimensionEvent event) {
        if (event.getEntity().level().isClientSide || !(event.getEntity() instanceof AbstractPiglin piglin) || !piglin.getPersistentData().hasUUID("owner")) {
            return;
        }

        for (Player player : PIGLIN_BEHAVIOR_MAP.keySet()) {
            if (PIGLIN_BEHAVIOR_MAP.get(player).contains(piglin)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void playerDimensionTravel(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity().level().isClientSide || !PIGLIN_BEHAVIOR_MAP.containsKey(event.getEntity())) {
            return;
        }

        Player player = event.getEntity();
        ServerLevel level = player.getServer().getLevel(event.getTo());

        Set<AbstractPiglin> piglins = new HashSet<>(PIGLIN_BEHAVIOR_MAP.get(player));
        PIGLIN_BEHAVIOR_MAP.get(player).clear();

        for (AbstractPiglin piglin : piglins) {
            if (piglin == null || !piglin.isAlive()) {
                continue;
            }
            AbstractPiglin newPiglin = (AbstractPiglin) piglin.changeDimension(level, PIGLIN_TELEPORTER);
            PIGLIN_BEHAVIOR_MAP.get(player).add(newPiglin);
        }
    }

    public static class PiglinTeleporter implements ITeleporter {

    }
}