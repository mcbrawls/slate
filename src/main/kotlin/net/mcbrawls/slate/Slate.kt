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
    var screenHandlerType: ScreenHandlerType<*> = ScreenHandlerType.GENERIC_9X6
        set(value) {
            field = value
            tiles = TileGrid.create(value)
        }

    var title: Text = Text.empty()
    var tiles: TileGrid = TileGrid.create(screenHandlerType)

    var callbackHandler: SlateCallbackHandler = SlateCallbackHandler()

    var canBeClosed: Boolean = true
    var parent: Slate? = null

    var handledSlate: HandledSlate? = null
    val player: ServerPlayerEntity? get() = handledSlate?.player

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

    fun onOpen(player: ServerPlayerEntity, handledSlate: HandledSlate) {
        callbackHandler.collectCallbacks<SlateOpenCallback>().invoke(this, player)

        val screenHandler = handledSlate.screenHandler
        screenHandler.clearOffhandSlotClient()
    }

    fun onTick(player: ServerPlayerEntity) {
        callbackHandler.collectCallbacks<SlateTickCallback>().invoke(this, player)
    }

    fun onClosed(player: ServerPlayerEntity) {
        callbackHandler.collectCallbacks<SlateCloseCallback>().invoke(this, player)
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
    open fun onAnvilInput(player: ServerPlayerEntity, input: String) {
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
