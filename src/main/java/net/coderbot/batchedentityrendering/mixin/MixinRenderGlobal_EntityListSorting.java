package net.coderbot.batchedentityrendering.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal_EntityListSorting {
    @Shadow
    private WorldClient world;

    @Inject(method = "renderEntities", at = @At("HEAD"), cancellable = true)
    private void batchedentityrendering$sortEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        Profiler profiler = Minecraft.getMinecraft().profiler;
        profiler.startSection("sortEntityList");

        List<Entity> sortedList = new ArrayList<>(world.loadedEntityList);
        sortedList.sort(Comparator.comparing(entity -> entity.getClass().getName()));

        world.loadedEntityList.clear();
        world.loadedEntityList.addAll(sortedList);

        profiler.endSection();
    }
}
