package net.mcbrawls.slate

import net.mcbrawls.slate.callback.SlateCallback
import net.mcbrawls.slate.callback.SlateClosedCallback
import net.mcbrawls.slate.callback.SlateOpenCallback
import net.mcbrawls.slate.callback.SlateTickCallback
import net.mcbrawls.slate.screen.SlateScreenHandler
import net.mcbrawls.slate.screen.SlateScreenHandlerFactory
import net.mcbrawls.slate.tile.TileGrid
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class Slate {
    /**
     * The screen handler type to be sent to the client.
     */
    var screenHandlerType: ScreenHandlerType<*> = ScreenHandlerType.GENERIC_9X6

    /**
     * The title of the interface on the client.
     */
    var title: Text = Text.empty()

    /**
     * The grid of tiles handled by this slate.
     */
    val tiles: TileGrid = TileGrid(9, 6)

    /**
     * Whether this slate can be manually closed by the player.
     */
    var canBeClosed: Boolean = true

    /**
     * The parent of this slate.
     */
    var parent: Slate? = null

    /**
     * The handled slate for when this slate is open.
     */
    var handledSlate: HandledSlate? = null

    val callbacks: MutableList<SlateCallback> = mutableListOf()

    /**
     * Adds callbacks to this slate.
     */
    fun addCallbacks(vararg callbacksIn: SlateCallback) {
        callbacks.addAll(callbacksIn)
    }

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
                handledSlate = HandledSlate(player, syncId, screenHandler)
                return true
            }
        }

        return false
    }

    open fun onOpen(player: ServerPlayerEntity) {
        callbacks.filterIsInstance<SlateOpenCallback>().forEach { callback ->
            callback.onStatus(this, player)
        }
    }

    fun tick() {
        callbacks.filterIsInstance<SlateTickCallback>().forEach { callback ->
            handledSlate?.also { handledSlate ->
                callback.onStatus(this, handledSlate.player)
            }
        }
    }

    fun onClosed(player: ServerPlayerEntity) {
        callbacks.filterIsInstance<SlateClosedCallback>().forEach { callback ->
            callback.onStatus(this, player)
        }

        handledSlate = null
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Slate::class.java)

        /**
         * Builds a default slate.
         */
        inline fun slate(builder: Slate.() -> Unit = {}): Slate {
            return Slate().apply(builder)
        }

        /**
         * Opens a slate for the given player.
         * @return whether the slate was opened successfully
         */
        fun openSlate(slate: Slate, player: ServerPlayerEntity): Boolean {
            if (player.isDisconnected) {
                return false
            }

            if (slate.open(player)) {
                slate.onOpen(player)
            }

            return false
        }
    }
}
