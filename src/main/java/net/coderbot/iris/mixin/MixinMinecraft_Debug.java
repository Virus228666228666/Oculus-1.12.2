package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft_Debug {
    @Inject(method = "createDisplay", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;create(Lorg/lwjgl/opengl/PixelFormat;)V"),
            require = 1, cancellable = true)
    private void onCreate(CallbackInfo ci) throws LWJGLException {
        if (Iris.getIrisConfig().areDebugOptionsEnabled()) {
            Display.create((new PixelFormat()).withDepthBits(24), null, new ContextAttribs().withDebug(true));
            ci.cancel();
        }
    }
}
