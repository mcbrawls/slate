package net.mcbrawls.slate.layer.callback

import net.mcbrawls.slate.layer.SlateLayer

/**
 * Handles callbacks for instances of [SlateLayer].
 */
open class SlateLayerCallbackHandler {
    val callbacks: MutableList<SlateLayerCallback> = mutableListOf()

    /**
     * Adds a callback invoked on the layer every game tick.
     */
    fun onTick(callback: SlateLayerTickCallback) {
        callbacks.add(callback)
    }

    /**
     * Adds a callback invoked on the layer if a page changes.
     */
    fun onPageChange(callback: SlateLayerPageChangeCallback) {
        callbacks.add(callback)
    }

    /**
     * Combines all callbacks for the given type into one callable object.
     */
    inline fun <reified T : SlateLayerCallback> collectCallbacks(): SlateLayerCallback {
        return SlateLayerCallback { slate, layer, player ->
            callbacks
                .filterIsInstance<T>()
                .forEach { callback -> callback.invoke(slate, layer, player) }
        }
    }
}
