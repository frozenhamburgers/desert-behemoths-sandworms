package net.jelly.jelllymod.event.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import net.jelly.jelllymod.entity.IK.KinematicChainEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.CommandEvent;

public class FabrikBackwardCommand extends CommandEvent {

    public FabrikBackwardCommand(ParseResults<CommandSourceStack> parse) {
        super(parse);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("fabrikbackward").executes(FabrikBackwardCommand::execute));
    }
    private static int execute(CommandContext<CommandSourceStack> command){
        if(command.getSource().getEntity() instanceof KinematicChainEntity){
            KinematicChainEntity caster = (KinematicChainEntity) command.getSource().getEntity();
            caster.fabrikBackward();
        }
        return Command.SINGLE_SUCCESS;
    }

}