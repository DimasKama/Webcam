package ru.dimaskama.webcam.client.neoforge;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import ru.dimaskama.webcam.client.WebcamModClient;
import ru.dimaskama.webcam.client.neoforge.screen.AdvancedWebcamScreen;
import ru.dimaskama.webcam.client.screen.WebcamScreen;

import javax.annotation.Nonnull;

public class WebcamNeoForgeConfigScreenFactory implements IConfigScreenFactory {

    @Override
    @Nonnull
    public Screen createScreen(@Nonnull ModContainer container, @Nonnull Screen modListScreen) {
        return WebcamModClient.canUseAdvancedConfigScreen()
                ? new AdvancedWebcamScreen().create(modListScreen)
                : new WebcamScreen(modListScreen);
    }

}
