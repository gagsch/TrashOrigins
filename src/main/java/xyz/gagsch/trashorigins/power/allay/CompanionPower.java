package xyz.gagsch.trashorigins.power.allay;

import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.util.Pair;
import io.github.edwinmindcraft.apoli.api.power.configuration.ConfiguredPower;
import io.github.edwinmindcraft.apoli.api.power.factory.power.ActiveCooldownPowerFactory;
import io.github.edwinmindcraft.apoli.common.power.configuration.ActiveSelfConfiguration;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("all")
public class CompanionPower extends ActiveCooldownPowerFactory.Simple<ActiveSelfConfiguration> {
    public static final HashMap<UUID, Allay> ALLAYS = new HashMap(); // player UUID to allay

    public CompanionPower() {
        super(ActiveSelfConfiguration.CODEC);
    }

    @Override
    protected void execute(ConfiguredPower<ActiveSelfConfiguration, ?> configuredPower, Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity.level().isClientSide)
            return;
        
        removeAllay(entity);
        addAllay(entity);
    }

    @Override
    public void onAdded(ConfiguredPower<ActiveSelfConfiguration, ?> configuration, Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity.level().isClientSide)
            return;

        addAllay(entity);
    }

    @Override
    public void onRemoved(ConfiguredPower<ActiveSelfConfiguration, ?> configuration, Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity.level().isClientSide)
            return;

        removeAllay(entity);
    }

    public void addAllay(Entity entity) {
        Level level = entity.level();
        Allay allay = EntityType.ALLAY.create(level);

        if (allay != null) {
            allay.moveTo(entity.getX(), entity.getY(), entity.getZ());
            allay.getBrain().setMemory(MemoryModuleType.LIKED_PLAYER, entity.getUUID());
            allay.setInvulnerable(true);
            level.addFreshEntity(allay);

            removeAllay(entity);
            ALLAYS.put(entity.getUUID(), allay);
        }
    }

    public void removeAllay(Entity entity) {
        Allay allay = ALLAYS.get(entity.getUUID());

        if (allay != null) {
            ALLAYS.remove(allay);
            allay.kill();
        }
    }
}
