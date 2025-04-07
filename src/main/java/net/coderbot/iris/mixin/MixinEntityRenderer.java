package net.coderbot.iris.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import nanolive.utils.RenderGlobalExtended;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.pipeline.HandRenderer;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Shadow
    @Final
    private Minecraft mc;

    // Begin shader rendering after buffers have been cleared.
    // At this point we've ensured that Minecraft's main framebuffer is cleared.
    // This is important or else very odd issues will happen with shaders that have a final pass that doesn't write to
    // all pixels.
    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;clear(I)V", ordinal = 0, shift = At.Shift.AFTER))
    private void iris$beginLevelRender(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        CapturedRenderingState.INSTANCE.setGbufferModelView(new Matrix4f(ClippingHelperImpl.getInstance().modelviewMatrix));
        CapturedRenderingState.INSTANCE.setGbufferProjection(new Matrix4f(ClippingHelperImpl.getInstance().projectionMatrix));
        CapturedRenderingState.INSTANCE.setTickDelta(partialTicks);
        SystemTimeUniforms.COUNTER.beginFrame();
        SystemTimeUniforms.TIMER.beginFrame(finishTimeNano);

        Program.unbind();

        RenderGlobalExtended renderGlobalExtended = (RenderGlobalExtended) this.mc.renderGlobal;
        renderGlobalExtended.setPipeline(Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension()));

        renderGlobalExtended.getPipeline().beginLevelRendering();
    }

    // Inject a bit early so that we can end our rendering before mods like VoxelMap (which inject at RETURN)
    // render their waypoint beams.
    @Inject(method = "renderWorldPass", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void iris$endLevelRender(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        HandRenderer.INSTANCE.renderTranslucent(partialTicks, this.mc.entityRenderer);
        mc.profiler.endStartSection("iris_final");

        RenderGlobalExtended renderGlobalExtended = (RenderGlobalExtended) this.mc.renderGlobal;
        renderGlobalExtended.getPipeline().finalizeLevelRendering();
        renderGlobalExtended.setPipeline(null);

        Program.unbind();
    }

    // Setup shadow terrain & render shadows before the main terrain setup. We need to do things in this order to
    // avoid breaking other mods such as Light Overlay: https://github.com/IrisShaders/Iris/issues/1356
    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;setupTerrain(Lnet/minecraft/entity/Entity;DLnet/minecraft/client/renderer/culling/ICamera;IZ)V"))
    private void iris$renderTerrainShadows(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci, @Local ICamera icamera) {
        ((RenderGlobalExtended) this.mc.renderGlobal).getPipeline().renderShadows((RenderGlobalAccessor) this.mc.renderGlobal, icamera);
    }

    @Redirect(method = "renderWorldPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;renderDistanceChunks:I"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/ICamera;setPosition(DDD)V")))
    private int iris$alwaysRenderSky(GameSettings instance) {
        return Math.max(instance.renderDistanceChunks, 4);
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderSky(FI)V", shift = At.Shift.AFTER))
    private void iris$endSky(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        ((RenderGlobalExtended) this.mc.renderGlobal).getPipeline().setPhase(WorldRenderingPhase.NONE);
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderCloudsCheck(Lnet/minecraft/client/renderer/RenderGlobal;FIDDD)V"))
    private void iris$beginClouds(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        ((RenderGlobalExtended) this.mc.renderGlobal).getPipeline().setPhase(WorldRenderingPhase.CLOUDS);
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderCloudsCheck(Lnet/minecraft/client/renderer/RenderGlobal;FIDDD)V", shift = At.Shift.AFTER))
    private void iris$endClouds(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        ((RenderGlobalExtended) this.mc.renderGlobal).getPipeline().setPhase(WorldRenderingPhase.NONE);
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderWorldBorder(Lnet/minecraft/entity/Entity;F)V"))
    private void iris$beginWorldBorder(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        ((RenderGlobalExtended) this.mc.renderGlobal).getPipeline().setPhase(WorldRenderingPhase.WORLD_BORDER);
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderWorldBorder(Lnet/minecraft/entity/Entity;F)V", shift = At.Shift.AFTER))
    private void iris$endWorldBorder(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        ((RenderGlobalExtended) this.mc.renderGlobal).getPipeline().setPhase(WorldRenderingPhase.NONE);
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;renderDebug(FJ)V"))
    private void iris$setDebugRenderStage(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        ((RenderGlobalExtended) this.mc.renderGlobal).getPipeline().setPhase(WorldRenderingPhase.DEBUG);
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;renderDebug(FJ)V", shift = At.Shift.AFTER))
    private void iris$resetDebugRenderStage(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        ((RenderGlobalExtended) this.mc.renderGlobal).getPipeline().setPhase(WorldRenderingPhase.NONE);
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderRainSnow(F)V"))
    private void iris$beginWeather(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        ((RenderGlobalExtended) this.mc.renderGlobal).getPipeline().setPhase(WorldRenderingPhase.RAIN_SNOW);
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderRainSnow(F)V", shift = At.Shift.AFTER))
    private void iris$endWeather(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        ((RenderGlobalExtended) this.mc.renderGlobal).getPipeline().setPhase(WorldRenderingPhase.NONE);
    }

}
