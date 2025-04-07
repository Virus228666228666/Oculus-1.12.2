package net.coderbot.iris.mixin.vertices;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.vertices.ImmediateState;
import net.coderbot.iris.vertices.IrisVertexFormats;

@Mixin(BufferBuilder.class)
public abstract class MixinVertexFormat {
    @Shadow
    private VertexFormat vertexFormat;

    @Inject(method = "begin", at = @At("HEAD"), cancellable = true)
    private void iris$onBegin(int drawMode, VertexFormat format, CallbackInfo ci) {
        if (BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() && ImmediateState.renderWithExtendedVertexFormat) {
            if (format == DefaultVertexFormats.BLOCK || format == DefaultVertexFormats.POSITION_TEX_LMAP_COLOR) {
                this.vertexFormat = IrisVertexFormats.TERRAIN;
            } else if (format == DefaultVertexFormats.ITEM) {
                this.vertexFormat = IrisVertexFormats.ENTITY;
            }
        }
    }

    @Inject(method = "endVertex", at = @At("HEAD"))
    private void iris$onEndVertex(CallbackInfo ci) {
        if (BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() && ImmediateState.renderWithExtendedVertexFormat) {
        }
    }

    @Inject(method = "reset", at = @At("HEAD"))
    private void iris$onReset(CallbackInfo ci) {
        if (BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() && ImmediateState.renderWithExtendedVertexFormat) {
        }
    }
}
