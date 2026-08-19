package net.jelly.sandworm_mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.jelly.sandworm_mod.worldevents.SpiceBlowWorldEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import team.lodestar.lodestone.handlers.WorldEventHandler;

// Manual test harness for the Spice Blow world event's phases/visuals - see
// plans/spice_blow_harvesting_plan.md section 1.
public class SpiceBlowCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spiceblow")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawn")
                        .then(Commands.argument("size", FloatArgumentType.floatArg(0.1f))
                                .executes(ctx -> spawn(ctx.getSource(), FloatArgumentType.getFloat(ctx, "size"), ctx.getSource().getPosition()))
                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                        .executes(ctx -> spawn(ctx.getSource(), FloatArgumentType.getFloat(ctx, "size"), Vec3Argument.getVec3(ctx, "pos")))))));
    }

    private static int spawn(CommandSourceStack source, float size, Vec3 pos) {
        SpiceBlowWorldEvent event = new SpiceBlowWorldEvent().setPosition(pos).setSize(size);
        event.start(source.getLevel());
        event.setDirty();
        WorldEventHandler.addWorldEvent(source.getLevel(), event);
        source.sendSuccess(() -> Component.literal("Spawned spice blow (size " + size + ") at " + pos), true);
        return 1;
    }
}
