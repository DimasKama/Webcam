package ru.dimaskama.webcam.client.fabric.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import ru.dimaskama.webcam.client.WebcamModClient;
import ru.dimaskama.webcam.client.fabric.screen.AdvancedWebcamScreen;
import ru.dimaskama.webcam.client.screen.WebcamScreen;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (WebcamModClient.canUseAdvancedConfigScreen()) {
            return parent -> new AdvancedWebcamScreen().create(parent);
        }
        return WebcamScreen::new;
    }

}
