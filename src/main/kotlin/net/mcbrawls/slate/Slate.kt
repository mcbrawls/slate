package net.mcbrawls.slate

import net.mcbrawls.slate.callback.ChildSlateCloseCallback
import net.mcbrawls.slate.callback.SlateCloseCallback
import net.mcbrawls.slate.callback.SlateOpenCallback
import net.mcbrawls.slate.callback.SlateTickCallback
import net.mcbrawls.slate.callback.handler.SlateCallbackHandler
import net.mcbrawls.slate.layer.PagedSlateLayer
import net.mcbrawls.slate.layer.SlateLayer
import net.mcbrawls.slate.screen.SlateScreenHandler
import net.mcbrawls.slate.screen.SlateScreenHandlerFactory
import net.mcbrawls.slate.screen.slot.TileClickContext
import net.mcbrawls.slate.tile.HandledTileGrid
import net.mcbrawls.slate.tile.Tile
import net.mcbrawls.slate.tile.TileGrid
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.OptionalInt

typealias MinecraftUnit = net.minecraft.util.Unit

open class Slate {
    /**
     * An identifiable key for this slate. Use how you wish.
     */
    open var key: String? = null

    /**
     * The title of the screen handler.
     */
    open var title: Text = Text.empty()

    /**
     * The base tile grid of this slate.
     */
    open var tiles: HandledTileGrid = TileGrid.create(ScreenHandlerType.GENERIC_9X6)

    /**
     * Layers displayed on top of the base tile grid.
     */
    val layers: MutableList<LayerWithIndex> = mutableListOf()

    /**
     * Handles all callbacks for this slate.
     */
    open var callbackHandler: SlateCallbackHandler = SlateCallbackHandler()

    /**
     * Whether this slate can be closed manually by the player.
     */
    open var canPlayerClose: Boolean = true

    /**
     * Whether this slate can or should be closed naturally at all.
     */
    open var canBeClosed: Boolean = true

    /**
     * The parent of this slate, which can be returned to.
     */
    var parent: Slate? = null

    /**
     * Whether this slate should be synced.
     */
    var dirty: Boolean = true

    val screenHandlerType: ScreenHandlerType<*> get() = tiles.screenHandlerType

    private var handledSlate: HandledSlate<out Slate>? = null

    val size: Int get() = tiles.size

    /**
     * Provides a factory for setting up callbacks of this slate.
     */
    inline fun callbacks(factory: SlateCallbackHandler.() -> Unit) {
        callbackHandler = callbackHandler.apply(factory)
    }

    /**
     * Modifies the tile grid on this slate.
     */
    inline fun tiles(action: HandledTileGrid.() -> Unit) {
        action.invoke(tiles)
    }

    /**
     * Adds a layer to this slate.
     * @return the created layer
     */
    inline fun layer(
        index: Int,
        width: Int,
        height: Int,
        factory: SlateLayer.Factory = SlateLayer.Factory(::SlateLayer),
        builder: SlateLayer.() -> Unit,
    ) : SlateLayer {
        val layer = factory.create(width, height)
        layer.apply(builder)
        layers.add(LayerWithIndex(index, layer))
        return layer
    }

    /**
     * Adds a paged layer to this slate.
     * @return the created layer
     */
    inline fun pagedLayer(
        index: Int,
        width: Int,
        height: Int,
        maxCount: Int,
        crossinline slotFactory: (PagedSlateLayer, Int) -> Tile?,
        builder: SlateLayer.() -> Unit = {},
    ) : PagedSlateLayer {
        val layer = object : PagedSlateLayer(maxCount, width, height) {
            override fun createTile(index: Int): Tile? {
                return slotFactory.invoke(this, index)
            }
        }

        layer.apply(builder)
        layers.add(LayerWithIndex(index, layer))

        return layer
    }

    /**
     * Builds a slate with this slate as the parent.
     */
    inline fun subslate(factory: () -> Slate = ::Slate, builder: Slate.() -> Unit = {}): Slate {
        val slate = factory.invoke()
        slate.parent = this
        return slate.apply(builder)
    }

    /**
     * Modifies the given slate to be a subslate of this slate.
     */
    fun subslate(slate: Slate): Slate {
        slate.parent = this
        return slate
    }

    operator fun get(tileIndex: Int): Tile? {
        val x = tileIndex % tiles.width
        val y = tileIndex / tiles.width

        val layerTile = layers
            .asReversed()
            .firstNotNullOfOrNull { (layerIndex, layer) ->
                // convert global position to layer-local position
                val layerStartX = layerIndex % tiles.width
                val layerStartY = layerIndex / tiles.width
                val layerX = x - layerStartX
                val layerY = y - layerStartY

                // check if the position is within layer bounds
                if (layerX in 0 until layer.width && layerY in 0 until layer.height) {
                    layer.tiles[layerY * layer.width + layerX]
                } else {
                    null
                }
            }

        return layerTile ?: tiles[tileIndex]
    }

    internal fun onOpen(player: ServerPlayerEntity, handledSlate: HandledSlate<out Slate>) {
        // invoke callbacks
        callbackHandler.collectCallbacks<SlateOpenCallback>().invoke(this, player)

        // manually clear offhand slot as vanilla does not handle this
        val screenHandler = handledSlate.screenHandler
        screenHandler.clearOffhandSlotClient()

        dirty = true
    }

    internal fun onTick(player: ServerPlayerEntity) {
        if (dirty) {
            handledSlate?.screenHandler?.syncState()
            dirty = false
        }

        // invoke callbacks
        callbackHandler.collectCallbacks<SlateTickCallback>().invoke(this, player)

        // layer ticks
        layers.forEach { indexedLayer ->
            indexedLayer.layer.onTick(this, player)
        }
    }

    internal fun onClosed(player: ServerPlayerEntity) {
        if (handledSlate != null) {
            // clean up
            player.currentScreenHandler.syncState()
            handledSlate = null

            // invoke callbacks
            callbackHandler.collectCallbacks<SlateCloseCallback>().invoke(this, player)

            parent?.also { firstParent ->
                val parents: List<Slate> = buildList {
                    add(firstParent)

                    // add nested parents
                    var nestedParent = firstParent.parent
                    while (nestedParent != null) {
                        add(nestedParent)
                        nestedParent = nestedParent.parent
                    }
                }

                parents.forEach { parent ->
                    parent.callbackHandler.collectCallbacks<ChildSlateCloseCallback>().invoke(this, player)
                }
            }
        }
    }

    /**
     * Called when any slot is clicked on the client.
     */
    open fun onSlotClicked(context: TileClickContext) {
        context.tile?.also { tile ->
            val clickType = context.clickType
            val callback = tile.collectClickCallbacks(clickType)
            callback.onClick(this, tile, context)
        }
    }

    /**
     * Called when the client input changes.
     */
    internal fun onAnvilInput(player: ServerPlayerEntity, input: String) {
        callbackHandler.collectInputCallbacks().onInput(this, player, input)
    }

    /**
     * Opens a slate for the given player.
     * @return whether the slate was opened successfully
     */
    open fun open(player: ServerPlayerEntity): Boolean {
        if (handledSlate != null) {
            if (player != handledSlate?.player) {
                logger.debug("Tried to reopen already opened slate for different player: {}, {}", this, handledSlate)
            } else {
                logger.debug("Tried to reopen already opened slate: {}, {}", this, handledSlate)
            }

            return false
        }

        // open handled screen
        val maybeSyncId = openHandledScreen(player)
        val syncId = if (maybeSyncId.isPresent) {
            maybeSyncId.orElseThrow()
        } else {
            return false
        }

        // store and return, if successful
        val screenHandler = player.currentScreenHandler as? SlateScreenHandler<*>
        if (screenHandler != null) {
            val handled = HandledSlate(player, syncId, screenHandler)
            handledSlate = handled
            onOpen(player, handled)
            return true
        }

        return false
    }

    /**
     * Opens this slate at the end of the current tick.
     */
    fun openSoon(player: ServerPlayerEntity): Boolean {
        if (handledSlate != null) {
            return false
        }

        val slatePlayer = player as SlatePlayer
        slatePlayer.setSoonSlate(this)

        return true
    }

    /**
     * Opens the handled screen for this player.
     * @return an optional sync id integer
     */
    open fun openHandledScreen(player: ServerPlayerEntity): OptionalInt {
        return player.openHandledScreen(SlateScreenHandlerFactory.create(this))
    }

    /**
     * Closes this slate if it is open.
     */
    fun close(player: ServerPlayerEntity): Boolean {
        handledSlate?.also { handledState ->
            val handler = handledState.screenHandler
            if (player.currentScreenHandler == handler) {
                player.closeHandledScreen()
                return true
            }
        }

        return false
    }

    /**
     * Opens the parent of this slate, if present.
     */
    fun openParent(player: ServerPlayerEntity): Boolean {
        parent?.also { parent -> return parent.open(player) }
        return false
    }

    /**
     * Removes a layer from this slate.
     * @return if the layer was removed
     */
    fun removeLayer(layer: SlateLayer): Boolean {
        return layers.removeIf { it.layer == layer }
    }

    override fun toString(): String {
        return "Slate{${tiles.screenHandlerType}:$title, $tiles}"
    }

    /**
     * The stored data for an active layer.
     */
    data class LayerWithIndex(
        val index: Int,
        val layer: SlateLayer,
    )

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Slate::class.java)

        /**
         * Builds a default slate.
         */
        inline fun slate(factory: () -> Slate = ::Slate, builder: Slate.() -> Unit = {}): Slate {
            return factory.invoke().apply(builder)
        }

        /**
         * Checks if this slate (nullable) has the checked key.
         */
        fun Slate?.hasKey(checkedKey: String): Boolean {
            return this != null && key == checkedKey
        }
    }
}
