package ru.dimaskama.webcam.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.client.DisplayingVideo;
import ru.dimaskama.webcam.client.KnownSourceClient;
import ru.dimaskama.webcam.client.WebcamModClient;
import ru.dimaskama.webcam.client.config.BlockedSources;
import ru.dimaskama.webcam.client.net.WebcamClient;
import ru.dimaskama.webcam.net.packet.AddBlockedSourceC2SPacket;
import ru.dimaskama.webcam.net.packet.RemoveBlockedSourceC2SPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class PlayersWebcamsList extends ContainerObjectSelectionList<PlayersWebcamsList.Entry> {

    private final Runnable dirtyAction;
    private final int rowWidth;
    private Predicate<KnownSourceClient> filter;

    public PlayersWebcamsList(Minecraft minecraft, Runnable dirtyAction, int rowWidth) {
        super(minecraft, 0, 0, 0, 36);
        this.dirtyAction = dirtyAction;
        this.rowWidth = rowWidth;
    }

    public void refresh(Predicate<KnownSourceClient> filter) {
        this.filter = filter;
        clearEntries();
        BlockedSources blocked = WebcamModClient.BLOCKED_SOURCES.getData();
        WebcamClient client = WebcamClient.getInstance();
        Map<UUID, KnownSourceClient> onServer;
        if (client != null) {
            onServer = client.getKnownSources();
        } else {
            onServer = null;
        }
        List<KnownSourceClient> allSources = new ArrayList<>();
        blocked.sources().forEach((uuid, name) -> {
            KnownSourceClient sourceOnServer = onServer != null ? onServer.get(uuid) : null;
            allSources.add(sourceOnServer != null
                    ? sourceOnServer
                    : new KnownSourceClient(uuid, name));
        });
        if (onServer != null) {
            onServer.values().forEach(source -> {
                if (!blocked.contains(source.getUuid())) {
                    allSources.add(source);
                }
            });
        }
        for (KnownSourceClient source : allSources) {
            if (filter.test(source)) {
                addEntry(new Entry(source, blocked.contains(source.getUuid())));
            }
        }
    }

    @Override
    public int getRowWidth() {
        return rowWidth;
    }

    public class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private static final ResourceLocation UNKNOWN_SPRITE = WebcamMod.id("unknown");
        private final KnownSourceClient source;
        private final List<AbstractWidget> children = new ArrayList<>();
        private final HideWebcamButton hideButton;

        public Entry(KnownSourceClient source, boolean blocked) {
            this.source = source;
            hideButton = new HideWebcamButton(blocked, b -> {
                WebcamClient client = WebcamClient.getInstance();
                if (b) {
                    WebcamModClient.BLOCKED_SOURCES.getData().add(source.getUuid(), source.getName());
                    if (client != null) {
                        if (client.isAuthenticated()) {
                            client.send(new AddBlockedSourceC2SPacket(source.getUuid()));
                        }
                        DisplayingVideo displayingVideo = client.getDisplayingVideos().remove(source.getUuid());
                        if (displayingVideo != null) {
                            displayingVideo.close();
                        }
                    }
                } else {
                    WebcamModClient.BLOCKED_SOURCES.getData().remove(source.getUuid());
                    if (client != null && client.isAuthenticated()) {
                        client.send(new RemoveBlockedSourceC2SPacket(source.getUuid()));
                    }
                }
                if (!filter.test(source)) {
                    removeEntry(this);
                }
                dirtyAction.run();
            });
            children.add(hideButton);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
            guiGraphics.fill(x, y, x + entryWidth - 4, y + entryHeight, 0x33000000);

            ResourceLocation customIcon = source.getCustomIcon();
            if (customIcon != null) {
                guiGraphics.blit(RenderType::guiTextured, customIcon, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
            } else {
                ClientPacketListener connection = Minecraft.getInstance().getConnection();
                PlayerInfo playerInfo = connection != null ? connection.getPlayerInfo(source.getUuid()) : null;
                PlayerSkin skin = playerInfo != null ? playerInfo.getSkin() : null;
                if (skin != null) {
                    PlayerFaceRenderer.draw(guiGraphics, skin, x, y, 32);
                } else {
                    guiGraphics.blitSprite(RenderType::guiTextured, UNKNOWN_SPRITE, x, y, 32, 32);
                }
            }

            guiGraphics.drawString(Minecraft.getInstance().font, source.getName(), x + 36, y + 4, 0xFFFFFFFF);

            hideButton.setRectangle(20, 20, x + entryWidth - 4 - 22, y + ((entryHeight - 20) >> 1));
            hideButton.render(guiGraphics, mouseX, mouseY, delta);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return children;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }

    }

}
