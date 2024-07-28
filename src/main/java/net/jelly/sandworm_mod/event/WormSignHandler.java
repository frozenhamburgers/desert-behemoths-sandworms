package net.jelly.sandworm_mod.event;

import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.capabilities.wormsign.WormSign;
import net.jelly.sandworm_mod.capabilities.wormsign.WormSignProvider;
import net.jelly.sandworm_mod.config.CommonConfigs;
import net.jelly.sandworm_mod.entity.IK.worm.WormChainEntity;
import net.jelly.sandworm_mod.entity.ModEntities;
import net.jelly.sandworm_mod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

import static net.jelly.sandworm_mod.helper.BiomeHelper.isDesertBiome;
import static net.jelly.sandworm_mod.helper.WarningSpawnHelper.spawnWorm;
import static net.jelly.sandworm_mod.helper.WarningSpawnHelper.warningScreenshake;

@Mod.EventBusSubscriber(modid = SandwormMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WormSignHandler {
    // WORMSIGN
    @SubscribeEvent
    public static void tickWS(TickEvent.PlayerTickEvent event) {
        int spawnWorm = CommonConfigs.SPAWNWORM_WORMSIGN.get();
        if(event.side == LogicalSide.CLIENT) return;
        Player player = event.player;

        // if not worm spawnable biome or underground
        if (!isDesertBiome(player.getServer().getLevel(player.level().dimension()), player.blockPosition())
                || player.level().getBrightness(LightLayer.SKY, player.blockPosition()) <= 0) {
            player.getCapability(WormSignProvider.WS).ifPresent(ws -> {
                decrementWormSign(1, ws);
            });
            return;
        }

        // if worm already present
        if (!player.level().getEntitiesOfClass(WormChainEntity.class,
                new AABB(player.position().add(600, 200, 600), player.position().subtract(600, 200, 600))).isEmpty()) {
            player.getCapability(WormSignProvider.WS).ifPresent(ws -> {
                ws.setStage(0);
                ws.setStageTimer(0);
                ws.subWS(2*spawnWorm);
                ws.setMultiplier(0);
                ws.setRespawnTimer(CommonConfigs.RESPAWN_DURATION.get()*40);
            });
            return;
        }

        //normal wormsign handling
        int softBoots = player.getItemBySlot(EquipmentSlot.FEET).getEnchantmentLevel(Enchantments.FALL_PROTECTION);
        player.getCapability(WormSignProvider.WS).ifPresent(ws -> {
            // System.out.println(ws.getWS());
            if(!ws.canRespawn()) {
                ws.decrementRespawnTimer();
                return;
            }

            if (ws.getSignTimer() < 200) {
                if (player.isSprinting()) {
                    incrementWormSign((4-softBoots), player, ws);
                }
                ws.addThisJumpTime(1);
                ws.addMultiplier(-0.01);
                if (ws.getSignTimer() == 0) decrementWormSign(1, ws);;

                // spawn sandworm
                if (ws.getWS() >= spawnWorm) {
                    ws.subWS(2*spawnWorm);
                    spawnWorm(player);
                }
            }
            ws.decrementSignTimer();
        });
    }

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if(event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        int softBoots = player.getItemBySlot(EquipmentSlot.FEET).getEnchantmentLevel(Enchantments.FALL_PROTECTION);
        if (!isDesertBiome(player.getServer().getLevel(player.level().dimension()), player.blockPosition())
                || player.level().getBrightness(LightLayer.SKY, player.blockPosition()) <= 0) return;

        player.getCapability(WormSignProvider.WS).ifPresent(ws -> {
            if (ws.getSignTimer() < 200) {
                float lastJump = ws.getLastJumpTime();
                float thisJump = ws.getThisJumpTime();
                float percentDiff = (Math.abs(lastJump - thisJump)) / ((lastJump + thisJump) / 2f);
                float similarity = 1 - percentDiff;
                ws.addMultiplier(similarity * 0.7);
                incrementWormSign((int) (80 * ws.getMultiplier() * (1-softBoots/16.0)), player, ws);
//                            System.out.println("add:" + (int) (40 * ws.getMultiplier()));
//                            System.out.println("multiplier:" + ws.getMultiplier());
                ws.setLastJumpTime(ws.getThisJumpTime());
                ws.setThisJumpTime(0);
            }
        });
    }


    private static void incrementWormSign(int add, Player player, WormSign ws) {
        int spawnWorm = CommonConfigs.SPAWNWORM_WORMSIGN.get();
        if (ws.getWS() < spawnWorm / 2 && (ws.getWS() + add) >= spawnWorm / 2) {
            warningScreenshake(player, 0.5, ModSounds.WORM_WARNING_1.get(), ws.getStage(), ws.getWS());
            ws.setStage(1);
            ws.setStageTimer(600);
            ws.setSignTimer();
        } else if (ws.getWS() < spawnWorm * 0.8 && (ws.getWS() + add) >= spawnWorm * 0.8) {
            warningScreenshake(player, 0.6, ModSounds.WORM_WARNING_2.get(), ws.getStage(), ws.getWS());
            ws.setStage(2);
            ws.setStageTimer(600);
            ws.setSignTimer();
        }
        ws.addWS(add);
    }

    private static void decrementWormSign(int decrement, WormSign ws) {
        int spawnWorm = CommonConfigs.SPAWNWORM_WORMSIGN.get();
        if(ws.getStage() == 0) {
            ws.subWS(decrement);
            ws.setStageTimer(0);
        }
        else if (ws.getStage() == 1) {
            if(ws.getWS() - decrement >= spawnWorm / 2) ws.subWS(decrement);
            else ws.setWS(spawnWorm/2);
            ws.decrementStageTimer();
            if(ws.dropStage()) ws.setStage(0);
        }
        else if (ws.getStage() == 2) {
            if(ws.getWS() - decrement >= spawnWorm * 0.8) ws.subWS(decrement);
            else ws.setWS((int)(spawnWorm * 0.8));
            ws.decrementStageTimer();
            if(ws.dropStage()) ws.setStage(1);
        }
    }



}
