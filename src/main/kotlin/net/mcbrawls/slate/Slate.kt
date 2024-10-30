package net.mcbrawls.slate

import net.mcbrawls.slate.callback.SlateCallbackHandler
import net.mcbrawls.slate.callback.SlateCloseCallback
import net.mcbrawls.slate.callback.SlateOpenCallback
import net.mcbrawls.slate.callback.SlateTickCallback
import net.mcbrawls.slate.screen.SlateScreenHandler
import net.mcbrawls.slate.screen.SlateScreenHandlerFactory
import net.mcbrawls.slate.screen.slot.TileClickContext
import net.mcbrawls.slate.tile.Tile
import net.mcbrawls.slate.tile.TileGrid
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.slf4j.Logger
import org.slf4j.LoggerFactory

typealias MinecraftUnit = net.minecraft.util.Unit

open class Slate {
    /**
     * The screen handler type sent to the client.
     * Affects what the client sees and how it communicates back to the server.
     */
    var screenHandlerType: ScreenHandlerType<*> = ScreenHandlerType.GENERIC_9X6
        set(value) {
            field = value
            tiles = TileGrid.create(value)
        }

    /**
     * The title of the screen handler.
     */
    var title: Text = Text.empty()

    /**
     * The base tile grid of this slate.
     */
    var tiles: TileGrid = TileGrid.create(screenHandlerType)

    /**
     * Handles all callbacks for this slate.
     */
    var callbackHandler: SlateCallbackHandler = SlateCallbackHandler()

    /**
     * Whether this slate can be closed manually by the player.
     */
    var canBeClosed: Boolean = true

    /**
     * The parent of this slate, which can be returned to.
     */
    var parent: Slate? = null

    private var handledSlate: HandledSlate? = null

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
    inline fun subslate(builder: Slate.() -> Unit = {}): Slate {
        val slate = Slate()
        slate.parent = this
        return slate.apply(builder)
    }

    operator fun get(tileIndex: Int): Tile? {
        return tiles[tileIndex]
    }

    internal fun onOpen(player: ServerPlayerEntity, handledSlate: HandledSlate) {
        // invoke callbacks
        callbackHandler.collectCallbacks<SlateOpenCallback>().invoke(this, player)

        // manually clear offhand slot as vanilla does not handle this
        val screenHandler = handledSlate.screenHandler
        screenHandler.clearOffhandSlotClient()
    }

    internal fun onTick(player: ServerPlayerEntity) {
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
    fun open(player: ServerPlayerEntity): Boolean {
        handledSlate?.also { handledSlate ->
            if (player != handledSlate.player) {
                logger.warn("Reopened already opened slate for different player: $this, $handledSlate")
            }
        }

        // open handled screen
        val syncId = runCatching {
            val syncId = player.openHandledScreen(SlateScreenHandlerFactory.create(this))
            syncId.orElseThrow()
        }.getOrNull()

        // store and return, if successful
        if (syncId != null) {
            val screenHandler = player.currentScreenHandler as? SlateScreenHandler
            if (screenHandler != null) {
                val handled = HandledSlate(player, syncId, screenHandler)
                handledSlate = handled
                onOpen(player, handled)
                return true
            }
        }

        return false
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
        return "Slate{$screenHandlerType:$title, $tiles}"
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Slate::class.java)

        /**
         * Builds a default slate.
         */
        inline fun slate(builder: Slate.() -> Unit = {}): Slate {
            return Slate().apply(builder)
        }
    }
}
