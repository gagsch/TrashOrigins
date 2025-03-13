package xyz.gagsch.trashorigins.powers.radiant;

import io.github.edwinmindcraft.apoli.api.ApoliAPI;
import io.github.edwinmindcraft.apoli.api.configuration.NoConfiguration;
import io.github.edwinmindcraft.apoli.api.power.configuration.ConfiguredPower;
import io.github.edwinmindcraft.apoli.api.power.factory.PowerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.gagsch.trashorigins.TrashOrigins;

import java.util.UUID;

@SuppressWarnings("all")
public class LightBoostPower extends PowerFactory<NoConfiguration> {
    public static final UUID SPEED_MOD_UUID = UUID.fromString("11111111-1111-1111-1111-112111111111");
    public static final UUID DAMAGE_MOD_UUID = UUID.fromString("22222222-2222-2222-2222-223222222222");
    public static final UUID ATTACK_SPEED_MOD_UUID = UUID.fromString("33333333-3333-3333-3333-334333333333");
    public static final ResourceLocation LIGHT_ENERGY_LOCATION = ResourceLocation.fromNamespaceAndPath(TrashOrigins.MODID, "radiant/light_energy");
    public static ConfiguredPower<?,?> LIGHT_ENERGY_POWER;

    public LightBoostPower() {
        super(NoConfiguration.CODEC);
        this.ticking(true);
    }

    @Override
    protected int tickInterval(NoConfiguration configuration, Entity entity) {
        return 3;
    }

    @Override
    public void tick(@NotNull ConfiguredPower<NoConfiguration, ?> configuration, Entity entity) {
        if (!entity.level().isClientSide && entity instanceof LivingEntity living) {
            float brightness = entity.level().getMaxLocalRawBrightness(entity.blockPosition());

            float multiplier = 0.25f + (brightness / 15) * 1.25f - 1;

            AttributeInstance speedAttr = living.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.removeModifier(SPEED_MOD_UUID);
                speedAttr.addTransientModifier(new AttributeModifier(SPEED_MOD_UUID, "Light speed boost", multiplier, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }

            AttributeInstance attackAttr = living.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackAttr != null) {
                attackAttr.removeModifier(DAMAGE_MOD_UUID);
                attackAttr.addTransientModifier(new AttributeModifier(DAMAGE_MOD_UUID, "Light damage boost", multiplier, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }

            AttributeInstance attackSpeedAttr = living.getAttribute(Attributes.ATTACK_SPEED);
            if (attackAttr != null) {
                attackAttr.removeModifier(ATTACK_SPEED_MOD_UUID);
                attackAttr.addTransientModifier(new AttributeModifier(ATTACK_SPEED_MOD_UUID, "Light attack speed boost", multiplier, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }

            LIGHT_ENERGY_POWER = ApoliAPI.getPowers().get(LIGHT_ENERGY_LOCATION);
            LIGHT_ENERGY_POWER.assign(entity, (int) (brightness * 6.67));
            ApoliAPI.synchronizePowerContainer(entity);
        }
    }
}
