package xyz.gagsch.trashorigins.power.allay;

import io.github.edwinmindcraft.apoli.api.ApoliAPI;
import io.github.edwinmindcraft.apoli.api.configuration.NoConfiguration;
import io.github.edwinmindcraft.apoli.api.power.configuration.ConfiguredPower;
import io.github.edwinmindcraft.apoli.api.power.factory.PowerFactory;
import io.github.edwinmindcraft.apoli.common.ApoliCommon;
import io.github.edwinmindcraft.calio.api.CalioAPI;
import io.github.edwinmindcraft.calio.api.registry.PlayerAbilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.OptionalInt;

import static xyz.gagsch.trashorigins.power.Powers.MUSIC_CHARGE_LOCATION;

@SuppressWarnings("all")
public class MusicFlightPower  extends PowerFactory<NoConfiguration> {
    public static ConfiguredPower<?,?> MUSIC_CHARGE_POWER;

    public MusicFlightPower() {
        super(NoConfiguration.CODEC);
        this.ticking(true);
    }

    @Override
    protected int tickInterval(NoConfiguration configuration, Entity entity) {
        return 20;
    }

    @Override
    public void tick(ConfiguredPower<NoConfiguration, ?> configuration, Entity entity) {
        if (!(entity instanceof Player player) || entity.level().isClientSide || entity.isPassenger())
            return;

        ChunkPos chunkPos = entity.chunkPosition();
        BlockPos blockPos = entity.blockPosition();

        // not mega readable but istg nested loops are the ugliest thing known to humanity
        // checks a radius of 4 chunks, jukeboxes sound radius is 64 blocks (which is 4 chunks)
        for (int i = 0; i < 81; i++) {
            int dx = i / 9 - 4;
            int dz = i % 9 - 4;

            LevelChunk chunk = entity.level().getChunk(chunkPos.x + dx, chunkPos.z + dz);

            if (jukeboxPlaying(chunk, blockPos)) {
                MUSIC_CHARGE_POWER = ApoliAPI.getPowers().get(MUSIC_CHARGE_LOCATION);
                OptionalInt charge = MUSIC_CHARGE_POWER.getValue(entity);
                MUSIC_CHARGE_POWER.assign(entity, charge.getAsInt() + 2);
                ApoliAPI.synchronizePowerContainer(entity);
            }
        }
    }

    @Override
    protected void onRemoved(NoConfiguration configuration, Entity entity) {
        CalioAPI.getAbilityHolder(entity).ifPresent(x -> x.revoke(PlayerAbilities.ALLOW_FLYING.get(), ApoliCommon.POWER_SOURCE));
    }

    public boolean jukeboxPlaying(LevelChunk chunk, BlockPos entityPos) {
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (be instanceof JukeboxBlockEntity jukebox && jukebox.isRecordPlaying() && jukebox.getBlockPos().distSqr(entityPos) < 4096) {
                return true;
            }
        }

        return false;
    }
}
