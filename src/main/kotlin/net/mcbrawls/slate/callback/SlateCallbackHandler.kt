package net.mcbrawls.slate.callback

import net.mcbrawls.slate.Slate

/**
 * Handles callbacks for instances of [Slate].
 */
open class SlateCallbackHandler {
    val callbacks: MutableList<SlateCallback> = mutableListOf()

    /**
     * Adds a callback invoked when the slate is opened.
     */
    fun onOpen(callback: SlateOpenCallback) {
        callbacks.add(callback)
    }

    /**
     * Adds a callback invoked on the slate every game tick.
     */
    fun onTick(callback: SlateTickCallback) {
        callbacks.add(callback)
    }

    /**
     * Adds a callback invoked when the slate is closed.
     */
    fun onClose(callback: SlateCloseCallback) {
        callbacks.add(callback)
    }

    /**
     * Combines all callbacks for the given type into one callable object.
     */
    inline fun <reified T : SlateCallback> collectCallbacks(): SlateCallback {
        return SlateCallback { slate, player ->
            callbacks
                .filterIsInstance<T>()
                .forEach { callback -> callback.invoke(slate, player) }
        }
    }
}
