package nanolive.compat;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL20;


import java.util.List;

public class VertexUtils {

//    POSITION("Position", (p_167043_, p_167044_, p_167045_, p_167046_, p_167047_, p_167048_) -> {
//            GlStateManager._enableVertexAttribArray(p_167048_);
//            GlStateManager._vertexAttribPointer(p_167048_, p_167043_, p_167044_, false, p_167045_, p_167046_);
//        }, (p_167040_, p_167041_) -> {
//            GlStateManager._disableVertexAttribArray(p_167041_);
//        })

    public static void setupBufferState(VertexFormat format, long offset) {
        int i = format.getIntegerSize();
        List<VertexFormatElement> list = format.getElements();

        for(int j = 0; j < list.size(); ++j) {
            GL20.glEnableVertexAttribArray(j);
            GL20.glVertexAttribPointer(j, ((VertexFormatElement)list.get(j)).getElementCount(), ((VertexFormatElement)list.get(j)).getType().getGlConstant(), false, i, (long)format.getOffset(j));
        }
    }

    public static void clearBufferState(VertexFormat format) {
        List<VertexFormatElement> list = format.getElements();

        for(int i = 0; i < list.size(); ++i) {
            VertexFormatElement vertexformatelement = (VertexFormatElement)list.get(i);
            GL20.glDisableVertexAttribArray(i);
        }
    }

}
