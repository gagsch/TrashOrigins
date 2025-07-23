package xyz.gagsch.trashorigins.power.piglin;

import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static xyz.gagsch.trashorigins.power.Powers.PIGLIN_GOLD_STANDARD_LOCATION;
import static xyz.gagsch.trashorigins.power.allay.CompanionPower.ALLAYS;

public class PiglinBehavior {
    public static final UUID DAMAGE_MOD_UUID = UUID.fromString("44444444-4444-4444-4444-445444444444");
    public static final Map<Player, List<AbstractPiglin>> PIGLIN_BEHAVIOR_MAP = new HashMap<>();

    public static boolean addBehavior(Player player, AbstractPiglin piglin, ItemStack itemstack) {
        if (piglin.isBaby() || PIGLIN_BEHAVIOR_MAP.containsKey(player) && (PIGLIN_BEHAVIOR_MAP.get(player).size() >= 10 || PIGLIN_BEHAVIOR_MAP.get(player).contains(piglin))) {
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
                if (!handler.hasPower(PIGLIN_GOLD_STANDARD_LOCATION))
                    return;

                ItemStack itemstack = player.getItemInHand(event.getHand());

                if (itemstack.getItem() == Items.PORKCHOP || itemstack.getItem() == Items.COOKED_PORKCHOP) {
                    itemstack.grow(-1);
                    piglin.heal(4);
                }
                else if (addBehavior(player, piglin, itemstack)) {
                    ((ServerPlayer) player).connection.send(new ClientboundLevelParticlesPacket(
                            ParticleTypes.HAPPY_VILLAGER, false,
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
            List<AbstractPiglin> list = PIGLIN_BEHAVIOR_MAP.get(player);


            if (player.isDeadOrDying()) {
                for (AbstractPiglin piglin : list) {
                    piglin.getPersistentData().remove("owner");
                    piglin.setImmuneToZombification(false);
                }
                list.clear();
                playerIterator.remove();
                continue;
            }

            AttributeInstance attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackAttr != null) {
                attackAttr.removeModifier(DAMAGE_MOD_UUID);
                attackAttr.addTransientModifier(new AttributeModifier(DAMAGE_MOD_UUID, "Strength in Numbers boost", (double) list.size() / 3, AttributeModifier.Operation.ADDITION));
            }

            Iterator<AbstractPiglin> iterator = list.iterator();

            LivingEntity target = getTarget(player);

            boolean targetExists = target != null && !target.isRemoved() && player.distanceToSqr(target) < 650;

            while (iterator.hasNext()) {
                AbstractPiglin abstractPiglin = iterator.next();

                if (abstractPiglin == null) {
                    iterator.remove();
                    continue;
                }

                if (!abstractPiglin.isAlive()) {
                    if (abstractPiglin.isDeadOrDying()) {
                        player.sendSystemMessage(abstractPiglin.getCombatTracker().getDeathMessage());
                    }
                    iterator.remove();
                    continue;
                }

                Brain<?> brain = abstractPiglin.getBrain();

                if (abstractPiglin.tickCount % 40 == 0) {
                    abstractPiglin.heal(1);
                }

                double distance = player.distanceToSqr(abstractPiglin);
                float baseWalkSpeed = player.getSpeed() * 10.5f;

                if (targetExists && !(target instanceof AbstractPiglin)) {
                    brain.setMemory(MemoryModuleType.ANGRY_AT, target.getUUID());
                } else {
                    brain.eraseMemory(MemoryModuleType.ANGRY_AT);
                    brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(player.blockPosition(), baseWalkSpeed, 4));
                    if (distance > 1600) {
                        abstractPiglin.teleportTo(player.getX(), player.getY(), player.getZ());
                    }
                }
            }
        }
    }

    private static @Nullable LivingEntity getTarget(Player player) {
        var lastHurt = player.getLastHurtMob();
        var lastHurtBy = player.getLastHurtByMob();

        if (lastHurt != null && !(lastHurt instanceof Player) && lastHurt.isAlive()) {
            return lastHurt;
        } else if (lastHurt instanceof Player && lastHurtBy == lastHurt && lastHurt.isAlive()) {
            return lastHurt;
        }


        return findNearestHostile(player);
    }
    private static @Nullable LivingEntity findNearestHostile(Player player) {
        Level level = player.level();
        AABB area = player.getBoundingBox().inflate(16);
        List<Mob> entities = level.getNearbyEntities(Mob.class, TargetingConditions.forCombat(), player, area);

        return entities.stream()
                .filter(e -> !(e instanceof AbstractPiglin) && e.isAlive() && player == e.getTarget())
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .orElse(null);
    }

    @SubscribeEvent
    public static void dimensionTravel(EntityTravelToDimensionEvent event) {
        if (event.getEntity().level().isClientSide() || event.getEntity() == null) {
            return;
        }

        if (event.getEntity() instanceof AbstractPiglin piglin && piglin.getPersistentData().hasUUID("owner")) {
            event.setCanceled(true);
            return;
        }

        if (event.getEntity() instanceof Allay allay && ALLAYS.containsValue(allay)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void playerDimensionTravel(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity().level().isClientSide())
            return;

        Player player = event.getEntity();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        ServerLevel level = Objects.requireNonNull(player.getServer()).getLevel(event.getTo());

        if (level == null)
            return;

        if (PIGLIN_BEHAVIOR_MAP.containsKey(player)) {
            Set<AbstractPiglin> piglins = new HashSet<>(PIGLIN_BEHAVIOR_MAP.get(player));
            PIGLIN_BEHAVIOR_MAP.get(player).clear();

            for (AbstractPiglin piglin : piglins) {
                if (piglin == null || !piglin.isAlive()) {
                    continue;
                }
                AbstractPiglin newPiglin = (AbstractPiglin) teleportTo(piglin, level, x, y, z);
                PIGLIN_BEHAVIOR_MAP.get(player).add(newPiglin);
            }

            return;
        }

        if (ALLAYS.containsKey(player.getUUID())) {
            Allay allay = ALLAYS.get(player.getUUID());

            if (allay == null)
                return;

            Allay newAllay = (Allay) teleportTo(allay, level, x, y, z);
            ALLAYS.put(player.getUUID(), newAllay);
        }
    }

    public static Entity teleportTo(Entity entity, ServerLevel level, double x, double y, double z) {
        if (level == entity.level()) {
            entity.moveTo(x, y, z, 0, 0);
        } else {
            entity.unRide();
            Entity newEntity = entity.getType().create(level);
            if (newEntity == null) {
                return entity;
            }

            newEntity.restoreFrom(entity);
            newEntity.moveTo(x, y, z, 0, 0);
            entity.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
            level.addDuringTeleport(newEntity);

            return newEntity;
        }

        return entity;
    }
}