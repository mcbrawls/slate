package net.mcbrawls.slate

import net.mcbrawls.slate.callback.SlateCallbackHandler
import net.mcbrawls.slate.callback.SlateCloseCallback
import net.mcbrawls.slate.callback.SlateOpenCallback
import net.mcbrawls.slate.callback.SlateTickCallback
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
     * The title of the screen handler.
     */
    open var title: Text = Text.empty()

    /**
     * The base tile grid of this slate.
     */
    open var tiles: HandledTileGrid = TileGrid.create(ScreenHandlerType.GENERIC_9X6)

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
     * Builds a slate with this slate as the parent.
     */
    inline fun subslate(factory: () -> Slate = ::Slate, builder: Slate.() -> Unit = {}): Slate {
        val slate = factory.invoke()
        slate.parent = this
        return slate.apply(builder)
    }

    operator fun get(tileIndex: Int): Tile? {
        return tiles[tileIndex]
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
    }

    internal fun onClosed(player: ServerPlayerEntity) {
        // invoke callbacks
        callbackHandler.collectCallbacks<SlateCloseCallback>().invoke(this, player)

        // clean up
        player.currentScreenHandler.syncState()
        handledSlate = null
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
        handledSlate?.also { handledSlate ->
            if (player != handledSlate.player) {
                logger.warn("Tried to reopen already opened slate for different player: $this, $handledSlate")
            } else {
                logger.warn("Tried to reopen already opened slate: $this, $handledSlate")
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

    override fun toString(): String {
        return "Slate{${tiles.screenHandlerType}:$title, $tiles}"
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Slate::class.java)

        /**
         * Builds a default slate.
         */
        inline fun slate(factory: () -> Slate = ::Slate, builder: Slate.() -> Unit = {}): Slate {
            return factory.invoke().apply(builder)
        }
    }
}
