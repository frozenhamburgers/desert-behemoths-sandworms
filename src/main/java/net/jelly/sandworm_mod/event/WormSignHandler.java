package net.jelly.sandworm_mod.event;

import com.mojang.logging.LogUtils;
import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.capabilities.wormsign.WormSign;
import net.jelly.sandworm_mod.capabilities.wormsign.WormSignProvider;
import net.jelly.sandworm_mod.config.ServerConfigs;
import net.jelly.sandworm_mod.entity.IK.worm.WormChainEntity;
import net.jelly.sandworm_mod.entity.ModEntities;
import net.jelly.sandworm_mod.helper.IPlayerMixinAccessor;
import net.jelly.sandworm_mod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.*;

import static net.jelly.sandworm_mod.helper.BiomeHelper.isDesertBiome;
import static net.jelly.sandworm_mod.helper.WarningSpawnHelper.spawnWorm;
import static net.jelly.sandworm_mod.helper.WarningSpawnHelper.warningScreenshake;

@Mod.EventBusSubscriber(modid = SandwormMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WormSignHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    // WORMSIGN
    @SubscribeEvent
    public static void tickWS(TickEvent.PlayerTickEvent event) {
        int spawnWorm = ServerConfigs.SPAWNWORM_WORMSIGN.get();
        if(event.side == LogicalSide.CLIENT) return;
        if(event.phase != TickEvent.Phase.END) return;
        Player player = event.player;

        // if not worm spawnable biome or underground
        if (!isDesertBiome(player.getServer().getLevel(player.level().dimension()), player.blockPosition())
                || player.level().getBrightness(LightLayer.SKY, player.blockPosition()) <= 0) {
            player.getCapability(WormSignProvider.WS).ifPresent(ws -> {
                decrementWormSign(1, player, ws);
            });
            return;
        }

        // if worm already present
        if (!player.level().getEntitiesOfClass(WormChainEntity.class,
                new AABB(player.position().add(800, 200, 800), player.position().subtract(800, 200, 800))).isEmpty()) {
            player.getCapability(WormSignProvider.WS).ifPresent(ws -> {
                ws.setStage(0);
                ws.setStageTimer(0);
                ws.subWS(2 * spawnWorm);
                ws.setMultiplier(0);
                ws.setRespawnTimer(ServerConfigs.RESPAWN_DURATION.get()*20);
            });
            return;
        }

        player.getCapability(WormSignProvider.WS).ifPresent(ws -> {
//            System.out.println(ws.getSignTimer());
            if(!ws.canRespawn()) {
                ws.decrementRespawnTimer();
                return;
            }

            if (ws.getSignTimer() < 200) {
                if (player.isPassenger()) {
                    Entity vehicle = player.getVehicle();
                    if (vehicle != null && isVehicleAllowed(vehicle)) {
                        handleVehicleTrigger(vehicle, player, ws);
                    }
                } else if (player.isSprinting()) {
                    handlePlayerSprintTrigger(player, ws);
                }

                ws.addThisJumpTime(1);
                ws.addMultiplier(-0.01);
                if (ws.getSignTimer() == 0) decrementWormSign(1, player, ws);

                // spawn sandworm
                if (ws.getWS() >= spawnWorm) {
                    ws.setWS(0);
                    spawnWorm(player);
                }
            }
            ws.decrementSignTimer();
            ws.decrementSeismometerGlitchTimer();
        });
    }

    // Rolling window of recent jump intervals, per player.
    private static final int WINDOW_SIZE = 4;
    private static final int MIN_VALID_INTERVAL = 6;
    private static final int MAX_VALID_INTERVAL = 40;
    private static final Map<UUID, Deque<Integer>> JUMP_HISTORY = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        int softBoots = player.getItemBySlot(EquipmentSlot.FEET).getEnchantmentLevel(Enchantments.FALL_PROTECTION);
        if (!isDesertBiome(player.getServer().getLevel(player.level().dimension()), player.blockPosition())
                || player.level().getBrightness(LightLayer.SKY, player.blockPosition()) <= 0) return;

        player.getCapability(WormSignProvider.WS).ifPresent(ws -> {
            if (ws.getSignTimer() >= 200) return;

            int thisInterval = (int) ws.getThisJumpTime();
            ws.setThisJumpTime(0);

            // Interval too fast/slow to be a genuine cadence attempt: clear the window
            // and apply a small, bounded penalty instead of letting bad data poison the average.
            if (thisInterval < MIN_VALID_INTERVAL || thisInterval > MAX_VALID_INTERVAL) {
                JUMP_HISTORY.computeIfAbsent(player.getUUID(), id -> new ArrayDeque<>()).clear();
                ws.setMultiplier(Math.max(0f, ws.getMultiplier() - 0.3f));
                return;
            }

            Deque<Integer> history = JUMP_HISTORY.computeIfAbsent(player.getUUID(), id -> new ArrayDeque<>());
            history.addLast(thisInterval);
            if (history.size() > WINDOW_SIZE) history.removeFirst();

            // Need at least two samples before "consistency" means anything.
            if (history.size() < 2) return;

            double mean = history.stream().mapToInt(Integer::intValue).average().orElse(0);
            double variance = history.stream().mapToDouble(i -> (i - mean) * (i - mean)).average().orElse(0);
            double stdDev = Math.sqrt(variance);
            double coefficientOfVariation = mean == 0 ? 1.0 : stdDev / mean; // guards the old div-by-zero

            // 0 = completely inconsistent, 1 = perfectly rhythmic
            float regularity = (float) Math.max(0, Math.min(1, 1 - coefficientOfVariation));
//            System.out.println("regularity: " + regularity);

            // Blend into the existing multiplier rather than overwrite it, so a single
            // rough jump doesn't erase an otherwise-good streak. Always clamped to [0,1].
            float newMultiplier = (float) Math.max(0f, Math.min(1f, ws.getMultiplier() * 0.7f + regularity * 0.3f));
            ws.setMultiplier(newMultiplier);

            int gain = (int) (80 * newMultiplier * (1 - softBoots / 16.0));
//            System.out.println("gain: " + gain);
            if (gain > 0) incrementWormSign(gain, player, ws);
        });
    }

    public static void clearHistory(UUID playerId) {
        JUMP_HISTORY.remove(playerId);
        LAST_VEHICLE_POS.remove(playerId);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearHistory(event.getEntity().getUUID());
    }


    private static void incrementWormSign(int add, Player player, WormSign ws) {
        int spawnWorm = ServerConfigs.SPAWNWORM_WORMSIGN.get();
        int wsAdded = ws.getWS() + add;
        if (ws.getWS() < spawnWorm / 2 && (wsAdded) >= spawnWorm / 2) {
            LOGGER.info("Sandworm reached stage 1: {}/{}, player: {}", wsAdded, spawnWorm, player.getDisplayName().getString());
            warningScreenshake(player, 0.5, ModSounds.WORM_WARNING_1.get(), ws.getStage(), ws.getWS(),
                    ServerConfigs.ENABLE_WARNING_MESSAGES.get() ? Component.translatable("msg.sandworm_mod.stage_1") : null);
            ws.setStage(1);
            ws.setStageTimer(600);
            ws.setSignTimer();
            ws.setSeismometerGlitchTimer(500);
        } else if (ws.getWS() < spawnWorm * 0.8 && (wsAdded) >= spawnWorm * 0.8) {
            LOGGER.info("Sandworm reached stage 2: {}/{}, player: {}", wsAdded, spawnWorm, player.getDisplayName().getString());
            warningScreenshake(player, 0.6, ModSounds.WORM_WARNING_2.get(), ws.getStage(), ws.getWS(),
                    ServerConfigs.ENABLE_WARNING_MESSAGES.get() ? Component.translatable("msg.sandworm_mod.stage_2") : null);
            ws.setStage(2);
            ws.setStageTimer(600);
            ws.setSignTimer();
            ws.setSeismometerGlitchTimer(500);
        }
        ws.addWS(add);
    }

    private static void decrementWormSign(int decrement, Player player, WormSign ws) {
        int spawnWorm = ServerConfigs.SPAWNWORM_WORMSIGN.get();
        if(ws.getStage() == 0) {
            ws.subWS(decrement);
            ws.setStageTimer(0);
        } else if (ws.getStage() == 1) {
            if (ws.getWS() - decrement >= spawnWorm / 2) {
                ws.subWS(decrement);
            } else {
                ws.setWS(spawnWorm/2);
            }
            ws.decrementStageTimer();
            if(ws.dropStage()) {
                ws.setStage(0);
                LOGGER.info("Sandworm dropped to stage 0: {}/{}, player: {}", ws.getWS(), spawnWorm, player.getDisplayName().getString());
            }
        } else if (ws.getStage() == 2) {
            if (ws.getWS() - decrement >= spawnWorm * 0.8) {
                ws.subWS(decrement);
            } else {
                ws.setWS((int)(spawnWorm * 0.8));
            }
            ws.decrementStageTimer();
            if (ws.dropStage()) {
                ws.setStage(1);
                LOGGER.info("Sandworm dropped to stage 1: {}/{}, player: {}", ws.getWS(), spawnWorm, player.getDisplayName().getString());
            }
        }
    }

    // Ground detection
    private static boolean isOnGround(Entity entity) {
        // Check if entity is on ground
        if (entity.onGround()) {
            return true;
        }

        // Additional check: check if there's a solid block below
        BlockPos below = entity.blockPosition().below();
        return entity.level().getBlockState(below).isSolid();
    }

    // Check if vehicle is allowed to trigger wormsign
    // Returns true only if vehicle is in whitelist AND not in blacklist
    private static boolean isVehicleAllowed(Entity vehicle) {
        String vehicleId = ForgeRegistries.ENTITY_TYPES.getKey(vehicle.getType()).toString();
        return ServerConfigs.isVehicleAllowed(vehicleId);
    }

    // Tracks each rider's vehicle position from the previous tick, since getDeltaMovement()
    // isn't reliable for rider-controlled vehicles (e.g. a tamed/saddled horse is driven by
    // its rider's client and the server doesn't populate its velocity the way it does for an
    // AI-driven vehicle, like an untamed horse bucking). Comparing position across ticks works
    // regardless of which side is authoritative for the vehicle's movement.
    private static final Map<UUID, Vec3> LAST_VEHICLE_POS = new HashMap<>();

    // Handle wormsign triggering for vehicles
    private static void handleVehicleTrigger(Entity vehicle, Player player, WormSign ws) {
        // Check if vehicle is on solid ground
        if (!isOnGround(vehicle)) {
            return;
        }

        Vec3 lastPos = LAST_VEHICLE_POS.put(player.getUUID(), vehicle.position());
        if (lastPos == null || vehicle.position().distanceToSqr(lastPos) < 0.01) {
            return;
        }

        int vehicleMultiplier = ServerConfigs.VEHICLE_TRIGGER_MULTIPLIER.get();
        incrementWormSign(vehicleMultiplier, player, ws);
    }

    private static void handlePlayerSprintTrigger(Player player, WormSign ws) {
        // Check if player is on ground (prevent flying)
        if (!isOnGround(player)) {
            return;
        }
        int enchantmentLevel = player.getItemBySlot(EquipmentSlot.FEET).getEnchantmentLevel(Enchantments.FALL_PROTECTION);
        int multiplier = ServerConfigs.PLAYER_TRIGGER_MULTIPLIER.get();
        int playerTrigger = Math.max(multiplier - enchantmentLevel, 0);
        if (playerTrigger > 0) {
            incrementWormSign(playerTrigger, player, ws);
        }
    }
}
