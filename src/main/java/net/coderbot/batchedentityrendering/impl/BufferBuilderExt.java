package net.coderbot.batchedentityrendering.impl;

import java.nio.ByteBuffer;

import net.minecraft.client.renderer.BufferBuilder;

public interface BufferBuilderExt {
    void setupBufferSlice(ByteBuffer buffer, BufferBuilder.State drawState);
    void teardownBufferSlice();
    void splitStrip();
}
