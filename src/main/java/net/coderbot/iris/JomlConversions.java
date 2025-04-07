package net.coderbot.iris;

import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.minecraft.util.math.Vec3d;
// import net.minecraft.world.phys.Vec3;

public class JomlConversions {
	public static Vector3d fromVec3(Vec3d vec) {
		return new Vector3d(vec.x, vec.y, vec.z);
	}

	// not sure what the use case for this is
	public static Vector4f toJoml(Vector4f v) {
		return new Vector4f(v.x(), v.y(), v.z(), v.w());
	}
}
