package net.coderbot.iris.gui.element.shaderoptions;

import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.screen.ShaderPackScreen;

public abstract class BaseEntry {
    protected final NavigationController navigation;

    protected BaseEntry(NavigationController navigation) {
        this.navigation = navigation;
    }

    public abstract boolean mouseClicked(int mouseX, int mouseY, int button);


    public abstract void drawEntry(ShaderPackScreen screen, int index, int x, int y, int slotWidth, int slotHeight, int mouseX, int mouseY, boolean isMouseOver);
}