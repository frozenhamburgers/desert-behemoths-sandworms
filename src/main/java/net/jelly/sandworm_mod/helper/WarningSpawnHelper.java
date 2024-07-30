package net.jelly.sandworm_mod.helper;

import net.jelly.sandworm_mod.advancements.AdvancementTriggerRegistry;
import net.jelly.sandworm_mod.capabilities.wormsign.WormSignProvider;
import net.jelly.sandworm_mod.entity.IK.worm.WormChainEntity;
import net.jelly.sandworm_mod.entity.ModEntities;
import net.jelly.sandworm_mod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import team.lodestar.lodestone.network.screenshake.ScreenshakePacket;
import team.lodestar.lodestone.registry.common.LodestonePacketRegistry;
import team.lodestar.lodestone.systems.easing.Easing;

import java.util.List;
import java.util.Random;

import static net.jelly.sandworm_mod.helper.BiomeHelper.isDesertBiome;

public class WarningSpawnHelper {
    public static void warningScreenshake(Player player, double strength, SoundEvent sound, int stage, int wormsign) {
        player.level().playSeededSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.MASTER, 12.5f,1,0);

        // send screenshake to all players within 200 blocks & unify their wormsigns & wormsign stages
        List<Player> nearbyPlayers = player.level().getNearbyPlayers(TargetingConditions.forNonCombat(), null,
                new AABB(player.position().add(200, 500, 200), player.position().subtract(200, 500, 200)));
        nearbyPlayers.forEach(p -> {
            LodestonePacketRegistry.LODESTONE_CHANNEL.send((PacketDistributor.PLAYER.with(() -> (ServerPlayer) p)),
                    new ScreenshakePacket(410).setEasing(Easing.SINE_IN_OUT).setIntensity((float)strength));
            player.getCapability(WormSignProvider.WS).ifPresent(ws -> {
                ws.setStage(stage);
                ws.setWS(wormsign);
                ws.setSignTimer();
            });
        });
    }

    public static void thumperWarning(Level level, Vec3 pos) {
        double strength = 0.6;
        level.playSeededSound(null, pos.x, pos.y, pos.z, ModSounds.WORM_WARNING_2.get(), SoundSource.MASTER, 12.5f,1,0);
        // send screenshake to all players within 200 blocks
        List<Player> nearbyPlayers = level.getNearbyPlayers(TargetingConditions.forNonCombat(), null,
                new AABB(pos.add(200, 500, 200), pos.subtract(200, 500, 200)));
        nearbyPlayers.forEach(p -> {
            LodestonePacketRegistry.LODESTONE_CHANNEL.send((PacketDistributor.PLAYER.with(() -> (ServerPlayer) p)),
                    new ScreenshakePacket(410).setEasing(Easing.SINE_IN_OUT).setIntensity((float)strength));
        });
    }



    private static Vec3 spawnPosOffset() {
        Random random = new Random();
        float xOffset = random.nextInt(100) + 100;
        if (random.nextBoolean()) xOffset *= -1;
        float zOffset = random.nextInt(100) + 100;
        if (random.nextBoolean()) zOffset *= -1;
        float yOffset = -5;
        return new Vec3(xOffset,yOffset,zOffset);
    }

    // WORM SPAWNING

    public static void spawnWorm(Player player) {
        if(player.level().isClientSide()) return;
        WormChainEntity sandWorm = new WormChainEntity(ModEntities.WORM_CHAIN.get(), player.level());
        Vec3 sandWormSpawnPos = player.position().add(spawnPosOffset());
        int spawnChecks = 0;
        while(!isDesertBiome(player.getServer().getLevel(player.level().dimension()).getLevel(), new BlockPos((int)sandWormSpawnPos.x, (int)sandWormSpawnPos.y, (int)sandWormSpawnPos.z))) {
            sandWormSpawnPos = player.position().add(spawnPosOffset());
            spawnChecks++;
            if(spawnChecks > 100) break;
        }
        sandWorm.moveTo(sandWormSpawnPos);
        sandWorm.setAggroTargetEntity(player);
        player.level().addFreshEntity(sandWorm);
        sandWorm.playSound(ModSounds.WORM_SPAWN.get(), 100, 1);
        AdvancementTriggerRegistry.SHAI_HULUD.trigger((ServerPlayer) player);
    }

    public static WormChainEntity spawnWormThumper(Level level, BlockPos bPos) {
        if(level.isClientSide()) return null;
        Vec3 pos = bPos.getCenter();
        WormChainEntity sandWorm = new WormChainEntity(ModEntities.WORM_CHAIN.get(), level);
        Vec3 sandWormSpawnPos = pos.add(spawnPosOffset());
        int spawnChecks = 0;
        while(!isDesertBiome(level.getServer().getLevel(level.dimension()), new BlockPos((int)sandWormSpawnPos.x, (int)sandWormSpawnPos.y, (int)sandWormSpawnPos.z))) {
            sandWormSpawnPos = pos.add(spawnPosOffset());
            spawnChecks++;
            if(spawnChecks > 100) break;
        }
        sandWorm.moveTo(sandWormSpawnPos);
        sandWorm.thumperTarget = pos;
        level.addFreshEntity(sandWorm);
        sandWorm.playSound(ModSounds.WORM_SPAWN.get(), 100, 1);
        return sandWorm;
    }

}
