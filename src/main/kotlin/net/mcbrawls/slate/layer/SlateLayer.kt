package net.mcbrawls.slate.layer

import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.layer.callback.SlateLayerCallbackHandler
import net.mcbrawls.slate.layer.callback.SlateLayerTickCallback
import net.mcbrawls.slate.tile.TileGrid
import net.minecraft.server.network.ServerPlayerEntity

/**
 * An instance of a layer on a slate.
 */
open class SlateLayer(
    val width: Int,
    val height: Int,
) {
    val tiles: TileGrid = TileGrid(width, height)

    /**
     * Handles all callbacks for this layer.
     */
    open var callbackHandler: SlateLayerCallbackHandler = SlateLayerCallbackHandler()

    /**
     * Provides a factory for setting up callbacks of this layer.
     */
    inline fun callbacks(factory: SlateLayerCallbackHandler.() -> Unit) {
        callbackHandler = callbackHandler.apply(factory)
    }

    internal fun onTick(slate: Slate, player: ServerPlayerEntity) {
        // invoke callbacks
        callbackHandler.collectCallbacks<SlateLayerTickCallback>().invoke(slate, this, player)
    }

    fun interface Factory {
        fun create(width: Int, height: Int): SlateLayer
    }
}
