package it.unimi.dsi.fastutil.floats;

import it.unimi.dsi.fastutil.SafeMath;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

@FunctionalInterface
public interface FloatConsumer extends Consumer<Float>, DoubleConsumer {
    void accept(float var1);

    @Deprecated
    default void accept(double t) {
        this.accept(SafeMath.safeDoubleToFloat(t));
    }

    @Deprecated
    default void accept(Float t) {
        this.accept(t.floatValue());
    }

    default FloatConsumer andThen(FloatConsumer after) {
        Objects.requireNonNull(after);
        return t -> {
            this.accept(t);
            after.accept(t);
        };
    }

    default FloatConsumer andThen(DoubleConsumer after) {
        return this.andThen(after instanceof FloatConsumer ? (FloatConsumer)after : after::accept);
    }
}
