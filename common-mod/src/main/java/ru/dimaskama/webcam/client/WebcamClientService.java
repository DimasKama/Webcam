package ru.dimaskama.webcam.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.message.Channel;
import ru.dimaskama.webcam.message.Message;
import ru.dimaskama.webcam.net.packet.Packet;

import javax.annotation.Nullable;

public interface WebcamClientService {

    boolean canSendToServer(Channel<?> channel);

    void sendToServer(Message message);

    void tickAdvancedConfigScreen(Minecraft minecraft);

    Screen createAdvancedConfigScreen(@Nullable Screen parent);

    RenderType createWebcamRenderType(String name, RenderPipeline renderPipeline, ResourceLocation textureId);

    void recordPacket(Packet packet);

}
