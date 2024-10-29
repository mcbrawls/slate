package net.mcbrawls.slate;

public interface SlatePlayer {
    default boolean openSlate(Slate slate) {
        throw new AssertionError();
    }
}
