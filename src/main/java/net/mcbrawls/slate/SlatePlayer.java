package net.mcbrawls.slate;

import net.mcbrawls.slate.screen.SlateScreenHandler;
import org.jetbrains.annotations.Nullable;

public interface SlatePlayer {
    default boolean openSlate(Slate slate) {
        throw new AssertionError();
    }

    @Nullable
    default Slate getSlate() {
        throw new AssertionError();
    }

    @Nullable
    default SlateScreenHandler<?> getSlateScreenHandler() {
        throw new AssertionError();
    }

    default boolean hasSlateOpen() {
        throw new AssertionError();
    }
}
