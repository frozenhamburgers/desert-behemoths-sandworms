package net.jelly.jelllymod.event;

import net.jelly.jelllymod.JellyMod;
import net.jelly.jelllymod.capabilities.wormsign.WormSign;
import net.jelly.jelllymod.capabilities.wormsign.WormSignProvider;
import net.jelly.jelllymod.entity.IK.worm.WormChainEntity;
import net.jelly.jelllymod.entity.IK.worm.WormHeadSegment;
import net.jelly.jelllymod.entity.ModEntities;
import net.jelly.jelllymod.event.commands.*;
import net.jelly.jelllymod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import team.lodestar.lodestone.network.screenshake.ScreenshakePacket;
import team.lodestar.lodestone.registry.common.LodestonePacketRegistry;
import team.lodestar.lodestone.systems.easing.Easing;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = JellyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventHandler {
    // COMMANDS
    // RegisterCommands is for server commands, RegisterClientCommands for client commands
    @SubscribeEvent
    public static void RegisterModCommands(RegisterCommandsEvent event) {
        // Register your custom command during the register commands event
        FabrikForwardCommand.register(event.getDispatcher());
        FabrikBackwardCommand.register(event.getDispatcher());
        WormBreachCommand.register(event.getDispatcher());
    }

    // ATTACH CAPABILITIES
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player) {
            if(!event.getObject().getCapability(WormSignProvider.WS).isPresent()) {
                event.addCapability(new ResourceLocation(JellyMod.MODID, "properties"), new WormSignProvider());
            }
        }
    }


    // WORMSIGN
    private static int spawnWorm = 1000;
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(event.side == LogicalSide.SERVER) {
            Player player = event.player;
            // if in worm spawnable biome
            if (isDesertBiome(player.getServer().getLevel(player.level().dimension()), player.blockPosition())) {
                // if no worm already present
                if (player.level().getEntitiesOfClass(WormChainEntity.class,
                        new AABB(player.position().add(400, 200, 400), player.position().subtract(400, 200, 400))).isEmpty()) {
                    player.getCapability(WormSignProvider.WS).ifPresent(ws -> {
                        // System.out.println(ws.getWS());
                        if (ws.getSignTimer() < 200) {
                            if (player.isSprinting()) {
                                incrementWormSign(2, player, ws);
                            }
                            ws.addThisJumpTime(1);
                            ws.addMultiplier(-0.01);
                            if (ws.getSignTimer() == 0) ws.subWS(1);

                            // spawn sandworm
                            if (ws.getWS() >= spawnWorm) {
                                ws.subWS(4000);
                                spawnWorm(player);
                            }
                        }
                        ws.decrementSignTimer();
                    });
                } else {
                    player.getCapability(WormSignProvider.WS).ifPresent(ws -> {
                        //System.out.println(ws.getWS());
                        ws.subWS(10);
                        ws.setMultiplier(0);
                    });
                }
            } else player.getCapability(WormSignProvider.WS).ifPresent(ws -> {
                ws.subWS(1);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if(!event.getEntity().level().isClientSide()) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (isDesertBiome(player.getServer().getLevel(player.level().dimension()), player.blockPosition())) {
                    player.getCapability(WormSignProvider.WS).ifPresent(ws -> {
                        if (ws.getSignTimer() < 200) {
                            float lastJump = ws.getLastJumpTime();
                            float thisJump = ws.getThisJumpTime();
                            float percentDiff = (Math.abs(lastJump - thisJump)) / ((lastJump + thisJump) / 2f);
                            float similarity = 1 - percentDiff;
                            ws.addMultiplier(similarity * 0.7);
                            incrementWormSign((int) (40 * ws.getMultiplier()), player, ws);
//                            System.out.println("add:" + (int) (40 * ws.getMultiplier()));
//                            System.out.println("multiplier:" + ws.getMultiplier());
                            ws.setLastJumpTime(ws.getThisJumpTime());
                            ws.setThisJumpTime(0);
                        }
                    });
                }
            }
        }
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

    private static boolean isDesertBiome(ServerLevel level, BlockPos blockPos) {
        return level.getBiomeManager().getBiome(blockPos).is(BiomeTags.SPAWNS_GOLD_RABBITS);
    }

    private static void incrementWormSign(int add, Player player, WormSign ws) {
        if (ws.getWS() < spawnWorm / 2 && (ws.getWS() + add) >= spawnWorm / 2) {
            warningScreenshake(player, 0.5, ModSounds.WORM_WARNING_1.get());
            ws.setSignTimer();
        } else if (ws.getWS() < spawnWorm * 0.8 && (ws.getWS() + add) >= spawnWorm * 0.8) {
            warningScreenshake(player, 0.6, ModSounds.WORM_WARNING_2.get());
            ws.setSignTimer();
        }
        ws.addWS(add);
    }

    private static void spawnWorm(Player player) {
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
    }

    private static void warningScreenshake(Player thisPlayer, double strength, SoundEvent sound) {
        thisPlayer.level().playSeededSound(null, thisPlayer.getX(), thisPlayer.getY(), thisPlayer.getZ(), sound, SoundSource.MASTER, 0.75f,1,0);
        LodestonePacketRegistry.LODESTONE_CHANNEL.send((PacketDistributor.PLAYER.with(() -> (ServerPlayer) thisPlayer)),
                new ScreenshakePacket(410).setEasing(Easing.SINE_IN_OUT).setIntensity((float)strength));
    }


    // EXPLOSIONS
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if(!event.getLevel().isClientSide()) {
            Vec3 pos = event.getExplosion().getPosition();
            List<WormHeadSegment> hitHeads = event.getLevel().getEntitiesOfClass(WormHeadSegment.class, new AABB(pos.x + 5, pos.y + 5, pos.z + 5, pos.x - 5, pos.y - 5, pos.z - 5));
            if (!hitHeads.isEmpty()) hitHeads.forEach(head -> {
                head.playSound(ModSounds.WORM_ROAR.get(), 10f, 1f);
                WormChainEntity wormChain = head.getOwner();
                if(wormChain != null) wormChain.blastHit();
            });
        }
    }
}

