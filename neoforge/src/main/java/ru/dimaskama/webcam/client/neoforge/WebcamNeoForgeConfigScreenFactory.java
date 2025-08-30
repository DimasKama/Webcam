package ru.dimaskama.webcam.client.neoforge;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;
import ru.dimaskama.webcam.client.WebcamModClient;
import ru.dimaskama.webcam.client.neoforge.screen.AdvancedWebcamScreen;
import ru.dimaskama.webcam.client.screen.WebcamScreen;

public class WebcamNeoForgeConfigScreenFactory implements IConfigScreenFactory {

    @Override
    @NotNull
    public Screen createScreen(@NotNull ModContainer container, @NotNull Screen modListScreen) {
        return WebcamModClient.canUseAdvancedConfigScreen()
                ? new AdvancedWebcamScreen().create(modListScreen)
                : new WebcamScreen(modListScreen);
    }

}
