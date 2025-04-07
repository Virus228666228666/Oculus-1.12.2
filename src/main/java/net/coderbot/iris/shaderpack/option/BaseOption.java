package net.coderbot.iris.shaderpack.option;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public abstract class BaseOption {
	@Nonnull
	private final OptionType type;
	@Nonnull
	private final String name;
	@Nullable
	private final String comment;

	BaseOption(@Nonnull OptionType type, @Nonnull String name, @Nullable String comment) {
		this.type = type;
		this.name = name;

		if (comment == null || comment.isEmpty()) {
			this.comment = null;
		} else {
			this.comment = comment;
		}
	}

	@Nonnull
	public OptionType getType() {
		return type;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	public Optional<String> getComment() {
		return Optional.ofNullable(comment);
	}
}
