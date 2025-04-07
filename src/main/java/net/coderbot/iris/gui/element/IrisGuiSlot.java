package net.coderbot.iris.gui.element;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Objects;

// TODO: look into GuiListExtended & GuiSelectStringEntries
public abstract class IrisGuiSlot extends GuiSlot {
    @Setter
    @Getter
    protected boolean renderBackground = true;
    boolean scrolling = false;

    protected IrisGuiSlot(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
        // Set Center Vertically to false
        this.centerListVertically = false;

    }

    @Override
    protected void drawContainerBackground(Tessellator tessellator) {
        if (this.renderBackground) {
            super.drawContainerBackground(tessellator);
        }
    }

    @Override
    protected int getScrollBarX() {
        // Position the scrollbar at the rightmost edge of the screen.
        // By default, the scrollbar is positioned moderately offset from the center.
        return this.width - 6;
    }

    @Override
    protected void drawSelectionBox(int x, int y, int mouseX, int mouseY, float partialTicks) {
        final int oldPadding = this.headerPadding;
        this.headerPadding = 2;
        super.drawSelectionBox(x, y, mouseX, mouseY, partialTicks);
        this.headerPadding = oldPadding;
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick, int mouseX, int mouseY) {
        // Do nothing
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!this.getEnabled/*enabled*/()) {
            return false;
        }
        final int size = this.getSize();
        final int scrollBarX = this.getScrollBarX();
        final int rightEdge = scrollBarX + 6;
        final int elementLeft = this.width / 2 - this.getListWidth() / 2;
        final int elementRight = this.width / 2 + this.getListWidth() / 2;
        final int relativeY = mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
        boolean handled = false;
        final boolean leftMouseDown = Mouse.isButtonDown(0);
        final boolean rightMouseDown = Mouse.isButtonDown(1);

        if (mouseX <= this.left || mouseX >= this.right || mouseY <= this.top || mouseY >= this.bottom) {
            return handled;
        }
        if (leftMouseDown && mouseX >= scrollBarX && mouseX <= rightEdge) {
            scrolling = true;
            this.initialClickY = mouseY;
        } else if ((leftMouseDown || rightMouseDown)) {
            final int index = relativeY / this.slotHeight;

            if (mouseX >= elementLeft && mouseX <= elementRight && index >= 0 && relativeY >= 0 && index < size) {
                final boolean doubleCLick = index == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L;

                if (index != 0) {
                    this.elementClicked(index, doubleCLick, mouseX, mouseY);
                    this.selectedElement = index;
                }

                this.lastClicked = Minecraft.getSystemTime();
            } else if (mouseX >= elementLeft && mouseX <= elementRight && relativeY < 0) {
                this.clickedHeader(mouseX - elementLeft, mouseY - this.top + (int) this.amountScrolled - 4);
            }
        }

        return handled;
    }

    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        scrolling = false;
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.drawBackground();
        final int scrollBarX = this.getScrollBarX();
        final int rightEdge = scrollBarX + 6;
        final byte offset = 4;

        // Scrollbar nonsense
        // todo: is there a way to modify scroll speed?
        if (scrolling) {
            this.amountScrolled += ((float) mouseY - this.initialClickY);
            this.initialClickY = mouseY;
        } else {
            try {
                for (; !this.mc.gameSettings.touchscreen && Mouse.next(); Objects.requireNonNull(this.mc.currentScreen).handleMouseInput()) {
                    int dWheel = Mouse.getEventDWheel();

                    if (dWheel != 0) {
                        if (dWheel > 0) {
                            dWheel = -1;
                        } else {
                            dWheel = 1;
                        }

                        this.amountScrolled += (dWheel * this.slotHeight / 2.0f);
                    }
                }
            } catch(IOException ie) {
                // todo debug message
            }
        }


        this.bindAmountScrolled();
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        drawContainerBackground(tessellator);
        final int elementRight = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
        final int relativeY = this.top + 4 - (int) this.amountScrolled;

        if (this.hasListHeader) {
            this.drawListHeader(elementRight, relativeY, tessellator);
        }

        this.drawSelectionBox(elementRight, relativeY, mouseX, mouseY, partialTicks);
        GlStateManager.disableDepth();
        this.overlayBackground(0, this.top, 255, 255);
        this.overlayBackground(this.bottom, this.height, 255, 255);
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 0, 1);
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableTexture2D();

        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(this.left, (this.top + offset), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
        buffer.pos(this.right, (this.top + offset), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
        buffer.pos(this.right, this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos(this.left, this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
        tessellator.draw();

        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(this.left, this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos(this.right, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos(this.right, (this.bottom - offset), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        buffer.pos(this.left, (this.bottom - offset), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        tessellator.draw();

        // Draw scrollbar if needed
        final int contentOverflow = this.getMaxScroll();
        if (contentOverflow > 0) {
            registerScrollButtons(7, 8);
            int scrollPosSize = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();

            if (scrollPosSize < 32) {
                scrollPosSize = 32;
            }

            if (scrollPosSize > this.bottom - this.top - 8) {
                scrollPosSize = this.bottom - this.top - 8;
            }

            int scrollPos = (int) this.amountScrolled * (this.bottom - this.top - scrollPosSize) / contentOverflow + this.top;

            if (scrollPos < this.top) {
                scrollPos = this.top;
            }

            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollBarX, this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 128).endVertex();
            buffer.pos(rightEdge, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 128).endVertex();
            buffer.pos(rightEdge, this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 128).endVertex();
            buffer.pos(scrollBarX, this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 128).endVertex();
            tessellator.draw();

            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollBarX, (scrollPos + scrollPosSize), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            buffer.pos(rightEdge, (scrollPos + scrollPosSize), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            buffer.pos(rightEdge, scrollPos, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            buffer.pos(scrollBarX, scrollPos, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            tessellator.draw();

            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollBarX, (scrollPos + scrollPosSize - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            buffer.pos((rightEdge - 1), (scrollPos + scrollPosSize - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            buffer.pos((rightEdge - 1), scrollPos, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            buffer.pos(scrollBarX, scrollPos, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
    }
}