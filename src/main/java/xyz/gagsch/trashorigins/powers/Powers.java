package xyz.gagsch.trashorigins.powers;

import io.github.edwinmindcraft.apoli.api.power.factory.PowerFactory;
import io.github.edwinmindcraft.apoli.api.registry.ApoliRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import xyz.gagsch.trashorigins.TrashOrigins;
import xyz.gagsch.trashorigins.powers.goat.RamPower;
import xyz.gagsch.trashorigins.powers.radiant.LightBoostPower;

public class Powers {
    public static final DeferredRegister<PowerFactory<?>> POWER_FACTORIES = DeferredRegister.create(ApoliRegistries.POWER_FACTORY_KEY.location(), TrashOrigins.MODID);

    public static final RegistryObject<LightBoostPower> LIGHT_BOOST = POWER_FACTORIES.register("light_boost", LightBoostPower::new);
    public static final RegistryObject<RamPower> RAM = POWER_FACTORIES.register("ram", RamPower::new);
}
