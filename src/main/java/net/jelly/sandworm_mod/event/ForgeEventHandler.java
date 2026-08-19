package net.jelly.sandworm_mod.event;

import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.advancements.AdvancementTriggerRegistry;
import net.jelly.sandworm_mod.command.SpiceBlowCommand;
import net.jelly.sandworm_mod.capabilities.wormsign.WormSignProvider;
import net.jelly.sandworm_mod.config.ServerConfigs;
import net.jelly.sandworm_mod.entity.IK.worm.WormHeadSegment;
import net.jelly.sandworm_mod.helper.IPlayerMixinAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.brewing.PlayerBrewedPotionEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

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
        SpiceBlowCommand.register(event.getDispatcher());
    }

    // ATTACH CAPABILITIES
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(WormSignProvider.WS).isPresent()) {
                event.addCapability(ResourceLocation.fromNamespaceAndPath(SandwormMod.MODID, "properties"), new WormSignProvider());
            }
        }
    }

    // EXPLOSIONS
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel().isClientSide()) return;
        Vec3 pos = event.getExplosion().getPosition();
        List<WormHeadSegment> hitHeads = event.getLevel().getEntitiesOfClass(WormHeadSegment.class, new AABB(pos.x + 5, pos.y + 5, pos.z + 5, pos.x - 5, pos.y - 5, pos.z - 5));
        if (!hitHeads.isEmpty()) hitHeads.forEach(head -> {
            head.hitSandwormHead((Player) event.getExplosion().getIndirectSourceEntity(), ServerConfigs.EXPLOSION_DAMAGE.get());
        });
    }


    @SubscribeEvent
    public static void brewPotion(PlayerBrewedPotionEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if(event.getStack().getTag().getBoolean("duneElixir")) AdvancementTriggerRegistry.DUNE_ELIXIR.trigger((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void playerFallEvent(LivingFallEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) return;
        IPlayerMixinAccessor player = (IPlayerMixinAccessor)event.getEntity();
        if(player.getGrappling()) {
            player.setGrappling(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (!event.side.isClient() && event.player.onGround()) {
            ((IPlayerMixinAccessor) event.player).tickGrapplingTimer();
            if(((IPlayerMixinAccessor) event.player).getGrapplingTimer() <= 0) {
                ((IPlayerMixinAccessor) event.player).setGrappling(false);
            }
        }
    }

}

