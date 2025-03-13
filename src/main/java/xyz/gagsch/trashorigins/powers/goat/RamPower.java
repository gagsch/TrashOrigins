package xyz.gagsch.trashorigins.powers.goat;

import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import io.github.edwinmindcraft.apoli.api.configuration.NoConfiguration;
import io.github.edwinmindcraft.apoli.api.power.configuration.ConfiguredPower;
import io.github.edwinmindcraft.apoli.api.power.factory.PowerFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import xyz.gagsch.trashorigins.TrashOrigins;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("all")
public class RamPower extends PowerFactory<NoConfiguration> {
    public static ConfiguredPower<?,?> RAM_ABILITY_POWER;
    public static final ResourceLocation RAM_ABILITY_LOCATION = ResourceLocation.fromNamespaceAndPath(TrashOrigins.MODID, "goat/ram_ram_action");
    public boolean hasHit = false;

    public RamPower() {
        super(NoConfiguration.CODEC);
        this.ticking(true);
    }

    @Override
    protected int tickInterval(NoConfiguration configuration, Entity entity) {
        return 1;
    }

    @Override
    public void tick(ConfiguredPower<NoConfiguration, ?> configuration, Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity.level().isClientSide || entity.isPassenger())
            return;

        AtomicInteger atomicResource = new AtomicInteger(-1);

        IPowerContainer.get(entity).ifPresent(container -> {
            container.getPower(RAM_ABILITY_LOCATION).value().getValue(living).ifPresent(atomicResource::set);
        });

        float resource = atomicResource.get();

        if (resource < 270) {
            hasHit = false;
            return;
        }
        else if (!isBlocking(living)) {
            hasHit = true;
            return;
        }
        else if (hasHit || living.isInWater()) {
            return;
        }

        int armor = living.getArmorValue();
        float velocityChange = (resource - 270) / (20f + armor * 1.5f) + 0.4f;

        if (!entity.onGround())
            velocityChange /= 2;

        Vec3 lookDir = entity.getLookAngle().normalize();
        Vec3 velocity = new Vec3(lookDir.x * velocityChange, entity.getDeltaMovement().y(), lookDir.z * velocityChange);

        entity.setDeltaMovement(velocity);
        entity.hurtMarked = true;

        LivingEntity nearest = entity.level().getNearestEntity(
                LivingEntity.class, TargetingConditions.DEFAULT, living,
                entity.getX(), entity.getY(), entity.getZ(),
                entity.getBoundingBox());

        if (nearest != null) {
            nearest.hurt(entity.damageSources().generic(), 6 + armor / 4);
            entity.level().playSound(null, entity.blockPosition(), SoundEvents.GOAT_RAM_IMPACT, SoundSource.PLAYERS, 1, 1);
            hasHit = true;
        }
    }

    public static boolean isBlocking(LivingEntity entity) {
        if (entity.isUsingItem() && !entity.getUseItem().isEmpty()) {
            Item item = entity.getUseItem().getItem();
            if (entity.getUseItem().canPerformAction(ToolActions.SHIELD_BLOCK)) {
                return true;
            }
        }
        return false;
    }
}
