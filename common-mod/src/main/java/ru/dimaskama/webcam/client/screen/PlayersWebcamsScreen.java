package ru.dimaskama.webcam.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import ru.dimaskama.webcam.client.KnownSourceClient;
import ru.dimaskama.webcam.client.WebcamModClient;
import ru.dimaskama.webcam.client.screen.widget.PlayersWebcamsList;

import java.util.Locale;
import java.util.function.Predicate;

public class PlayersWebcamsScreen extends Screen {

    private static final int MENU_WIDTH = 160;
    private static final int MENU_HEIGHT = 200;
    private final Screen parent;
    private final boolean shouldPause;

    private int menuX, menuY;
    private Tab selectedTab = Tab.ALL;
    private Button allTabButton;
    private Button hiddenTabButton;
    private EditBox search;
    private PlayersWebcamsList list;
    private boolean dirty;

    public PlayersWebcamsScreen(Screen parent, boolean shouldPause) {
        super(Component.translatable("webcam.screen.players_webcams"));
        this.parent = parent;
        this.shouldPause = shouldPause;
    }

    @Override
    protected void init() {
        menuX = (width - MENU_WIDTH) >> 1;
        menuY = (height - MENU_HEIGHT) >> 1;

        int widgetX = menuX + 3;
        int widgetWidth = MENU_WIDTH - 6;
        int widgetY = menuY + 20;

        int tabButtonWidth = (widgetWidth - 2) / Tab.values().length;

        if (allTabButton == null) {
            allTabButton = Button.builder(Component.translatable("webcam.screen.players_webcams.tab.all"), button -> {
                selectedTab = Tab.ALL;
                hiddenTabButton.active = true;
                button.active = false;
                refreshList();
            }).build();
            allTabButton.active = selectedTab != Tab.ALL;
        }
        allTabButton.setRectangle(tabButtonWidth, 16, widgetX + 1, widgetY);
        addRenderableWidget(allTabButton);

        if (hiddenTabButton == null) {
            hiddenTabButton = Button.builder(Component.translatable("webcam.screen.players_webcams.tab.hidden"), button -> {
                selectedTab = Tab.HIDDEN;
                allTabButton.active = true;
                button.active = false;
                refreshList();
            }).build();
            hiddenTabButton.active = selectedTab != Tab.HIDDEN;
        }
        hiddenTabButton.setRectangle(tabButtonWidth, 16, widgetX + 1 + tabButtonWidth, widgetY);
        addRenderableWidget(hiddenTabButton);

        widgetY += 17;

        if (search == null) {
            search = new EditBox(font, 0, 0, Component.empty());
            search.setMaxLength(16);
            search.setHint(Component.translatable("webcam.screen.players_webcams.search")
                    .withStyle(ChatFormatting.DARK_GRAY));
            search.setResponder(string -> refreshList());
        }
        search.setRectangle(widgetWidth - 2, 16, widgetX + 1, widgetY);
        addRenderableWidget(search);

        widgetY += 18;

        if (list == null) {
            list = new PlayersWebcamsList(minecraft, () -> dirty = true, MENU_WIDTH - 6);
            refreshList();
        }
        list.setRectangle(widgetWidth, MENU_HEIGHT - (widgetY - menuY) - 4, widgetX, widgetY);
        addRenderableWidget(list);
    }

    private void refreshList() {
        String searchText = search.getValue().strip().toLowerCase(Locale.ROOT);
        boolean searchEmpty = searchText.isBlank();
        list.refresh(searchEmpty
                ? selectedTab.filter
                : selectedTab.filter.and(s -> s.getNameForSearch().contains(searchText)));
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, WebcamScreen.BACKGROUND_SPRITE, menuX, menuY, MENU_WIDTH, MENU_HEIGHT);
        guiGraphics.drawString(font, title, (width - font.width(title)) >> 1, menuY + 7, 0xFF555555, false);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return shouldPause;
    }

    @Override
    public void removed() {
        if (dirty) {
            dirty = false;
            WebcamModClient.BLOCKED_SOURCES.save();
        }
    }

    private enum Tab {

        ALL(s -> true),
        HIDDEN(s -> WebcamModClient.BLOCKED_SOURCES.getData().contains(s.getUuid()));

        private final Predicate<KnownSourceClient> filter;

        Tab(Predicate<KnownSourceClient> filter) {
            this.filter = filter;
        }

    }

}
