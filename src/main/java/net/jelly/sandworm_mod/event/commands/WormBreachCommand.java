package net.jelly.sandworm_mod.event.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.worldevents.WormBreachWorldEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.CommandEvent;
import team.lodestar.lodestone.handlers.WorldEventHandler;

public class WormBreachCommand extends CommandEvent {

    public WormBreachCommand(ParseResults<CommandSourceStack> parse) {
        super(parse);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("wormbreach").executes(WormBreachCommand::execute));
    }

    private static final ParticleEmitterInfo SAND_SMOKE = new ParticleEmitterInfo(new ResourceLocation(SandwormMod.MODID, "sandsmoke"));
    private static int execute(CommandContext<CommandSourceStack> command){
        Level level = command.getSource().getLevel();
        Vec3 pos = command.getSource().getPosition();
//
//        SonicBoomWorldEvent breachEvent = new SonicBoomWorldEvent().spawnRipple(pos);
//        breachEvent.start(command.getSource().getUnsidedLevel());
//        breachEvent.setDirty();
//        WorldEventHandler.addWorldEvent(level, breachEvent);

//        Vector3f center = new Vector3f(0, 0, 0);
//        Vector3f color = new Vector3f(1, 0, 1);
//        SonicBoomPostProcessor.INSTANCE.addFxInstance(new SonicBoomFx(center, color));



        WormBreachWorldEvent breachEvent2 = new WormBreachWorldEvent().setPosition(pos);
        breachEvent2.start(command.getSource().getUnsidedLevel());
        breachEvent2.setDirty();
        WorldEventHandler.addWorldEvent(level, breachEvent2);

//        BlockPos blockPos = BlockPos.containing(pos.x, pos.y-1, pos.z);
//        BlockState blockState = level.getBlockState(blockPos);
//
//        System.out.println("spawning block particle: " + blockPos + ", " + blockState);
//        BlockParticleOption blockParticle = new BlockParticleOption(ParticleTypes.BLOCK, blockState);
//
//
//        for(int i = 0; i<360; i++) {
//            if(i%20 == 0) {
//                ((ServerLevel)level).sendParticles(
//                        blockParticle, // The particle option
//                        pos.x,         // X position
//                        pos.y,         // Y position
//                        pos.z,         // Z position
//                        10,            // Count of particles to spawn
//                        0.0,           // X offset for particle spread
//                        0.0,           // Y offset for particle spread
//                        0.0,           // Z offset for particle spread
//                        0.0            // Speed of the particle
//                );
////                (blockParticle, pos.x, pos.y+1, pos.z,
////                        Math.cos(i) * 0.25d, 0.25, Math.sin(i) * 0.25d);
//            }
//        }

        return Command.SINGLE_SUCCESS;
    }

}