package ru.dimaskama.webcam.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.command.WebcamconfigCommand;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class WebcamconfigFabricCommand implements CommandRegistrationCallback {

    private static final DynamicCommandExceptionType EXCEPTION = new DynamicCommandExceptionType(o -> new LiteralMessage(o.toString()));

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(literal(WebcamconfigCommand.COMMAND_NAME)
                .requires(Permissions.require(Webcam.WEBCAMCONFIG_COMMAND_PERMISSION, 2))
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
