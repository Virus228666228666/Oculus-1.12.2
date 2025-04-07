package net.coderbot.iris.gui.element.widget;

import java.util.Optional;

import net.coderbot.iris.shaderpack.option.menu.OptionMenuElement;
//import net.minecraft.network.chat.Component;
import net.minecraft.util.text.ITextComponent;

public abstract class CommentedElementWidget<T extends OptionMenuElement> extends AbstractElementWidget<T> {
	public CommentedElementWidget(T element) {
		super(element);
	}

	public abstract Optional<ITextComponent> getCommentTitle();

	public abstract Optional<ITextComponent> getCommentBody();
}
