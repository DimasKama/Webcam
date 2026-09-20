package ru.dimaskama.webcam.client;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import ru.dimaskama.webcam.message.Channel;
import ru.dimaskama.webcam.message.Message;
import ru.dimaskama.webcam.net.packet.Packet;

import javax.annotation.Nullable;

public interface WebcamClientService {

    boolean canSendToServer(Channel<?> channel);

    void sendToServer(Message message);

    void tickAdvancedConfigScreen(Minecraft minecraft);

    Screen createAdvancedConfigScreen(@Nullable Screen parent);

    RenderType createWebcamRenderType(String name, RenderPipeline renderPipeline, Identifier textureId);

    void recordPacket(Packet packet);

}
