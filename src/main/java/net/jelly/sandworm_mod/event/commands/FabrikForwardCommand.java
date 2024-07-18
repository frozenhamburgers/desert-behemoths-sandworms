package net.jelly.sandworm_mod.event.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import net.jelly.sandworm_mod.entity.IK.KinematicChainEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.CommandEvent;

// TESTING PURPOSES ONLY
public class FabrikForwardCommand extends CommandEvent {

    public FabrikForwardCommand(ParseResults<CommandSourceStack> parse) {
        super(parse);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("fabrikforward").executes(FabrikForwardCommand::execute));
    }
    private static int execute(CommandContext<CommandSourceStack> command){
        if(command.getSource().getEntity() instanceof KinematicChainEntity){
            KinematicChainEntity caster = (KinematicChainEntity) command.getSource().getEntity();
            caster.fabrikForward();
        }
        return Command.SINGLE_SUCCESS;
    }

}
