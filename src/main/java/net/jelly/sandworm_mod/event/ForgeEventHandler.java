package net.jelly.sandworm_mod.event;

import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.capabilities.wormsign.WormSign;
import net.jelly.sandworm_mod.capabilities.wormsign.WormSignProvider;
import net.jelly.sandworm_mod.config.CommonConfigs;
import net.jelly.sandworm_mod.entity.IK.worm.WormChainEntity;
import net.jelly.sandworm_mod.entity.IK.worm.WormHeadSegment;
import net.jelly.sandworm_mod.entity.ModEntities;
import net.jelly.sandworm_mod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
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

import static net.jelly.sandworm_mod.helper.BiomeHelper.isDesertBiome;

@Mod.EventBusSubscriber(modid = SandwormMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventHandler {
    // COMMANDS
    // RegisterCommands is for server commands, RegisterClientCommands for client commands
    @SubscribeEvent
    public static void RegisterModCommands(RegisterCommandsEvent event) {
        // Register your custom command during the register commands event
//        FabrikForwardCommand.register(event.getDispatcher());
//        FabrikBackwardCommand.register(event.getDispatcher());
//        WormBreachCommand.register(event.getDispatcher());
    }

    // ATTACH CAPABILITIES
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player) {
            if(!event.getObject().getCapability(WormSignProvider.WS).isPresent()) {
                event.addCapability(new ResourceLocation(SandwormMod.MODID, "properties"), new WormSignProvider());
            }
        }
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

