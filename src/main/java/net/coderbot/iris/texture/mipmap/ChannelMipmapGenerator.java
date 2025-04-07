package net.coderbot.iris.texture.mipmap;

public class ChannelMipmapGenerator extends AbstractMipmapGenerator {
	protected final BlendFunction redFunc;
	protected final BlendFunction greenFunc;
	protected final BlendFunction blueFunc;
	protected final BlendFunction alphaFunc;

	public ChannelMipmapGenerator(BlendFunction redFunc, BlendFunction greenFunc, BlendFunction blueFunc, BlendFunction alphaFunc) {
		this.redFunc = redFunc;
		this.greenFunc = greenFunc;
		this.blueFunc = blueFunc;
		this.alphaFunc = alphaFunc;
	}

	@Override
	public int blend(int c0, int c1, int c2, int c3) {
		return combine(
				alphaFunc.blend(
						(c0 >> 24) & 0xFF,
						(c1 >> 24) & 0xFF,
						(c2 >> 24) & 0xFF,
						(c3 >> 24) & 0xFF
				),
				blueFunc.blend(
						(c0 & 0xFF),
						(c1 & 0xFF),
						(c2 & 0xFF),
						(c3 & 0xFF)
				),
				greenFunc.blend(
						(c0 >> 8) & 0xFF,
						(c1 >> 8) & 0xFF,
						(c2 >> 8) & 0xFF,
						(c3 >> 8) & 0xFF
				),
				redFunc.blend(
						(c0 >> 16) & 0xFF,
						(c1 >> 16) & 0xFF,
						(c2 >> 16) & 0xFF,
						(c3 >> 16) & 0xFF
				)
		);
	}

	public interface BlendFunction {
		int blend(int v0, int v1, int v2, int v3);
	}

	private static int combine(int alpha, int blue, int green, int red) {
		alpha = (alpha & 0xFF) << 24;
		red = (red & 0xFF) << 16;
		green = (green & 0xFF) << 8;
		blue = blue & 0xFF;

		return alpha | red | green | blue;
	}
}
