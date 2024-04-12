package net.jelly.jelllymod.event.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.CommandEvent;
import team.lodestar.lodestone.handlers.WorldEventHandler;

public class WormBreachCommand extends CommandEvent {

    public WormBreachCommand(ParseResults<CommandSourceStack> parse) {
        super(parse);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("wormbreach").executes(WormBreachCommand::execute));
    }
    private static int execute(CommandContext<CommandSourceStack> command){
        System.out.println("cmd working");

        return Command.SINGLE_SUCCESS;
    }

}
