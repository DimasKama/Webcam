package ru.dimaskama.webcam;

import net.minecraft.commands.CommandSourceStack;

public interface WebcamModService {

    boolean isModLoaded(String modId);

    boolean checkWebcamconfigCommandPermission(CommandSourceStack commandSource);

}
