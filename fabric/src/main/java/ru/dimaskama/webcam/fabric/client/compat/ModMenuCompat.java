package ru.dimaskama.webcam.fabric.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import ru.dimaskama.webcam.fabric.client.screen.AdvancedWebcamScreen;
import ru.dimaskama.webcam.fabric.client.screen.WebcamScreen;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (AdvancedWebcamScreen.CAN_USE) {
            return parent -> new AdvancedWebcamScreen().create(parent);
        }
        return WebcamScreen::new;
    }

}
