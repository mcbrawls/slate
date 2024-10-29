package net.mcbrawls.slate

import net.mcbrawls.slate.screen.SlateScreenHandlerFactory
import net.mcbrawls.slate.tile.TileGrid
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

abstract class Slate {
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
    open val tileGrid: TileGrid = TileGrid(9, 6)

    /**
     * Whether this slate can be manually closed by the player.
     */
    var canBeClosed: Boolean = true

    /**
     * The parent of this slate.
     */
    var parent: Slate? = null

    fun sendScreen(player: ServerPlayerEntity): Boolean {
        player.openHandledScreen(SlateScreenHandlerFactory.create(this))
        return true
    }

    companion object {
        fun openSlate(slate: Slate, player: ServerPlayerEntity): Boolean {
            if (player.isDisconnected) {
                return false
            }

            return slate.sendScreen(player)
        }
    }
}
