package xyz.gagsch.trashorigins.power;

import io.github.edwinmindcraft.apoli.api.power.factory.PowerFactory;
import io.github.edwinmindcraft.apoli.api.registry.ApoliRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import xyz.gagsch.trashorigins.TrashOrigins;
import xyz.gagsch.trashorigins.power.goat.RamPower;
import xyz.gagsch.trashorigins.power.radiant.LightBoostPower;

public class Powers {
    public static final DeferredRegister<PowerFactory<?>> POWER_FACTORIES = DeferredRegister.create(ApoliRegistries.POWER_FACTORY_KEY.location(), TrashOrigins.MODID);

    public static final RegistryObject<LightBoostPower> LIGHT_BOOST = POWER_FACTORIES.register("light_boost", LightBoostPower::new);
    public static final RegistryObject<RamPower> RAM = POWER_FACTORIES.register("ram", RamPower::new);

    public static final ResourceLocation PIGLIN_NEUTRAL_LOCATION = new ResourceLocation(TrashOrigins.MODID, "piglin/piglin_neutral");
    public static final ResourceLocation PIGLIN_CAPITALISM_LOCATION = new ResourceLocation(TrashOrigins.MODID, "piglin/capitalism");
    public static final ResourceLocation BLESSED_LOCATION = new ResourceLocation(TrashOrigins.MODID, "piglin/blessed");
    public static final ResourceLocation RAM_ABILITY_LOCATION = new ResourceLocation(TrashOrigins.MODID, "goat/ram_ram_action");
    public static final ResourceLocation LIGHT_ENERGY_LOCATION = new ResourceLocation(TrashOrigins.MODID, "radiant/light_energy");
}
