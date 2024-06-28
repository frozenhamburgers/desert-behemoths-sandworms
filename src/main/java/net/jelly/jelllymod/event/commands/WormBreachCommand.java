package net.jelly.jelllymod.event.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.jelly.jelllymod.JellyMod;
import net.jelly.jelllymod.registry.client.ParticleRegistry;
import net.jelly.jelllymod.worldevents.WormBreachWorldEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.CommandEvent;
import team.lodestar.lodestone.handlers.WorldEventHandler;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;

public class WormBreachCommand extends CommandEvent {

    public WormBreachCommand(ParseResults<CommandSourceStack> parse) {
        super(parse);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("wormbreach").executes(WormBreachCommand::execute));
    }

    private static final ParticleEmitterInfo SAND_SMOKE = new ParticleEmitterInfo(new ResourceLocation(JellyMod.MODID, "sandsmoke"));
    private static int execute(CommandContext<CommandSourceStack> command){
        Level level = command.getSource().getPlayer().level();
        Vec3 pos = command.getSource().getPosition();

        WormBreachWorldEvent breachEvent = new WormBreachWorldEvent().setPosition(pos);
        breachEvent.start(command.getSource().getUnsidedLevel());
        breachEvent.setDirty();
        WorldEventHandler.addWorldEvent(level, breachEvent);


        return Command.SINGLE_SUCCESS;
    }

}
