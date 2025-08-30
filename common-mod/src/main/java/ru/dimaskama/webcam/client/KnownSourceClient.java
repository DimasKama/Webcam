package ru.dimaskama.webcam.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.util.UndashedUuid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.net.KnownSource;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.UUID;

public class KnownSourceClient {

    private final UUID uuid;
    private final String name;
    @Nullable
    private final NativeImage customIcon;
    @Nullable
    private ResourceLocation customIconId;
    private String nameForSearch;

    public KnownSourceClient(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        customIcon = null;
    }

    public KnownSourceClient(KnownSource source) {
        this.uuid = source.getUuid();
        this.name = source.getName();
        byte[] customIconPixels = source.getCustomIcon();
        NativeImage customIcon = null;
        if (customIconPixels != null) {
            customIcon = new NativeImage(KnownSource.ICON_DIMENSION, KnownSource.ICON_DIMENSION, false);
            for (int y = 0; y < KnownSource.ICON_DIMENSION; y++) {
                for (int x = 0; x < KnownSource.ICON_DIMENSION; x++) {
                    int i = (y * KnownSource.ICON_DIMENSION + x) * 4;
                    customIcon.setPixelRGBA(
                            x,
                            y,
                            ((customIconPixels[i] & 0xFF) << 24)
                                    | ((customIconPixels[i + 1] & 0xFF) << 16)
                                    | ((customIconPixels[i + 2] & 0xFF) << 8)
                                    | (customIconPixels[i + 3] & 0xFF)
                    );
                }
            }
        }
        this.customIcon = customIcon;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public ResourceLocation getCustomIcon() {
        if (customIcon != null && customIconId == null) {
            ResourceLocation id = WebcamMod.id("custom_icon_" + UndashedUuid.toString(uuid));
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(customIcon));
            customIconId = id;
        }
        return customIconId;
    }

    public String getNameForSearch() {
        if (nameForSearch == null) {
            nameForSearch = name.toLowerCase(Locale.ROOT);
        }
        return nameForSearch;
    }

    public void close() {
        if (customIcon != null) {
            Minecraft.getInstance().execute(() -> {
                if (customIconId != null) {
                    Minecraft.getInstance().getTextureManager().release(customIconId);
                } else {
                    customIcon.close();
                }
            });
        }
    }

}
