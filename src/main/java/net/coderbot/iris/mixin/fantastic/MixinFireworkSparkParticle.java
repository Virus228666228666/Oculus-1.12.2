package net.coderbot.iris.mixin.fantastic;

import net.minecraft.client.particle.ParticleSimpleAnimated;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.client.particle.ParticleFirework$Spark")
public abstract class MixinFireworkSparkParticle extends ParticleSimpleAnimated {
	public MixinFireworkSparkParticle(World worldIn, double x, double y, double z, int textureIdxIn, int numFrames, float yAccelIn) {
		super(worldIn, x, y, z, textureIdxIn, numFrames, yAccelIn);
	}
}
