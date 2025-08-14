package ru.dimaskama.webcam.spigot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.dimaskama.webcam.WebcamEvents;

public class WebcamPaperListener implements Listener {

    @EventHandler
    public void onPlayerDisconnected(PlayerQuitEvent event) {
        WebcamEvents.onPlayerDisconnected(event.getPlayer().getUniqueId());
    }

}
