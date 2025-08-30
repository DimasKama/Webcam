package ru.dimaskama.webcam.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import ru.dimaskama.webcam.WebcamMod;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class WebcamconfigModCommand {

    private static final DynamicCommandExceptionType EXCEPTION = new DynamicCommandExceptionType(o -> new LiteralMessage(o.toString()));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal(WebcamconfigCommand.COMMAND_NAME)
                .requires(src -> WebcamMod.getService().checkWebcamconfigCommandPermission(src))
                .then(argument("field", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            WebcamconfigCommand.suggestFields(builder.getRemaining()).forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            try {
                                String msg = WebcamconfigCommand.getField(StringArgumentType.getString(context, "field"));
                                context.getSource().sendSystemMessage(Component.literal(msg));
                            } catch (IllegalArgumentException e) {
                                throw EXCEPTION.create(e.getLocalizedMessage());
                            }
                            return 0;
                        })
                        .then(argument("new_value", StringArgumentType.word())
                                .executes(context -> {
                                    try {
                                        String msg = WebcamconfigCommand.setField(StringArgumentType.getString(context, "field"), StringArgumentType.getString(context, "new_value"));
                                        context.getSource().sendSystemMessage(Component.literal(msg));
                                    } catch (IllegalArgumentException e) {
                                        throw EXCEPTION.create(e.getLocalizedMessage());
                                    }
                                    return 1;
                                }))));
    }

}
