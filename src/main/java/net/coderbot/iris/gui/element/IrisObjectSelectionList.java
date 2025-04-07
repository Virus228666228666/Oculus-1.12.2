package net.coderbot.iris.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

import java.util.ArrayList;
import java.util.List;

public abstract class IrisObjectSelectionList<E extends GuiListExtended.IGuiListEntry> extends IrisGuiSlot {
	private final List<E> entries = new ArrayList<>();
	private E selectedEntry;

	public IrisObjectSelectionList(Minecraft client, int width, int height, int top, int bottom, int left, int right, int slotHeight) {
		super(client, width, height, top, bottom, slotHeight);
		this.left = left;
		this.right = right;
	}

	@Override
	protected int getSize() {
		return entries.size();
	}

	@Override
	protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
		select(slotIndex);
	}

	@Override
	protected boolean isSelected(int slotIndex) {
		return entries.get(slotIndex) == selectedEntry;
	}

	@Override
	protected void drawBackground() {
		// Draw the background if needed
	}

	@Override
	protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {
		entries.get(slotIndex).updatePosition(slotIndex, xPos, yPos, partialTicks);
		entries.get(slotIndex).drawEntry(slotIndex, xPos, yPos, getListWidth(), heightIn, mouseXIn, mouseYIn, mouseYIn >= yPos && mouseYIn <= yPos+heightIn, partialTicks);
	}

	public void select(int entry) {
		setSelected(entries.get(entry));
	}

	public void setSelected(E entry) {
		selectedEntry = entry;
	}

	protected void clearEntries() {
		this.entries.clear();
		selectedEntry = null;
	}

	protected void addEntry(E entry) {
		this.entries.add(entry);
	}

	public E getSelected() {
		return selectedEntry;
	}

	public E getEntry(int entry) {
		return entries.get(entry);
	}

	public int getListWidth() {
		return this.width;
	}

	public int getLeft() {
		return this.left;
	}

	public int getRight() {
		return this.right;
	}

	public int getTop() {
		return this.top;
	}

	public int getBottom() {
		return this.bottom;
	}
}