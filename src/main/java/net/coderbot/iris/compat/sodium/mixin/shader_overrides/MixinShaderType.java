package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL32;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import me.jellysquid.mods.sodium.client.gl.shader.ShaderType;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisShaderTypes;

@Mixin(ShaderType.class)
public class MixinShaderType {
	@SuppressWarnings("target")
	@Shadow(remap = false)
	@Final
	@Mutable
	private static ShaderType[] $VALUES;

	static {
		int baseOrdinal = $VALUES.length;

		IrisShaderTypes.GEOMETRY
				= ShaderTypeAccessor.createShaderType("GEOMETRY", baseOrdinal, GL32.GL_GEOMETRY_SHADER);

		$VALUES = ArrayUtils.addAll($VALUES, IrisShaderTypes.GEOMETRY);
	}
}
