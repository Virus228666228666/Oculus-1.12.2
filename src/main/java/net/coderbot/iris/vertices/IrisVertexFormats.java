package net.coderbot.iris.vertices;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class IrisVertexFormats {
	public static final VertexFormatElement ENTITY_ELEMENT;
	public static final VertexFormatElement MID_TEXTURE_ELEMENT;
	public static final VertexFormatElement TANGENT_ELEMENT;
	public static final VertexFormatElement MID_BLOCK_ELEMENT;

	public static final VertexFormat TERRAIN;
	public static final VertexFormat ENTITY;

	static {
		ENTITY_ELEMENT = new VertexFormatElement(11, VertexFormatElement.EnumType.SHORT, VertexFormatElement.EnumUsage.GENERIC, 2);
		MID_TEXTURE_ELEMENT = new VertexFormatElement(12, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.GENERIC, 2);
		TANGENT_ELEMENT = new VertexFormatElement(13, VertexFormatElement.EnumType.BYTE, VertexFormatElement.EnumUsage.GENERIC, 4);
		MID_BLOCK_ELEMENT = new VertexFormatElement(14, VertexFormatElement.EnumType.BYTE, VertexFormatElement.EnumUsage.GENERIC, 3);

		TERRAIN = new VertexFormat();
		ENTITY = new VertexFormat();

		TERRAIN.addElement(DefaultVertexFormats.POSITION_3F); // 12
		TERRAIN.addElement(DefaultVertexFormats.COLOR_4UB); // 16
		TERRAIN.addElement(DefaultVertexFormats.TEX_2F); // 24
		TERRAIN.addElement(DefaultVertexFormats.TEX_2S); // 28
		TERRAIN.addElement(DefaultVertexFormats.NORMAL_3B); // 31
		TERRAIN.addElement(DefaultVertexFormats.PADDING_1B); // 32
		TERRAIN.addElement(ENTITY_ELEMENT); // 36
		TERRAIN.addElement(MID_TEXTURE_ELEMENT); // 44
		TERRAIN.addElement(TANGENT_ELEMENT); // 48
		TERRAIN.addElement(MID_BLOCK_ELEMENT); // 51
		TERRAIN.addElement(DefaultVertexFormats.PADDING_1B); // 52

		ENTITY.addElement(DefaultVertexFormats.POSITION_3F); // 12
		ENTITY.addElement(DefaultVertexFormats.COLOR_4UB); // 16
		ENTITY.addElement(DefaultVertexFormats.TEX_2F); // 24
		ENTITY.addElement(DefaultVertexFormats.TEX_2S); // 28
		ENTITY.addElement(DefaultVertexFormats.TEX_2S); // 32
		ENTITY.addElement(DefaultVertexFormats.NORMAL_3B); // 35
		ENTITY.addElement(DefaultVertexFormats.PADDING_1B); // 36
		ENTITY.addElement(MID_TEXTURE_ELEMENT); // 44
		ENTITY.addElement(TANGENT_ELEMENT); // 48
	}
}
