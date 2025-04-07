package net.coderbot.iris.shaderpack;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * An absurdly simple class for storing pairs of strings because Java lacks pair / tuple types.
 */
public class StringPair {
	private final String key;
	private final String value;

	public StringPair(@Nonnull String key, @Nonnull String value) {
		this.key = Objects.requireNonNull(key);
		this.value = Objects.requireNonNull(value);
	}

	@Nonnull
	public String getKey() {
		return key;
	}

	@Nonnull
	public String getValue() {
		return value;
	}
}
