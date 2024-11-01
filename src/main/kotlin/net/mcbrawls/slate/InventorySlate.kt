package net.mcbrawls.slate

import net.mcbrawls.slate.screen.InventorySlateScreenHandler
import net.mcbrawls.slate.tile.TileGrid
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import java.util.OptionalInt

class InventorySlate : Slate() {
    override var tiles: TileGrid = TileGrid.create(ScreenHandlerType.GENERIC_9X1)

    override var canPlayerClose: Boolean = false
    override var canBeClosed: Boolean = false

    override fun openHandledScreen(player: ServerPlayerEntity): OptionalInt {
        if (player.currentScreenHandler != player.playerScreenHandler) {
            player.closeHandledScreen()
        }

        val screenHandler = InventorySlateScreenHandler(this, player, tiles.screenHandlerType)
        player.currentScreenHandler = screenHandler
        player.onScreenHandlerOpened(screenHandler)

        return OptionalInt.of(screenHandler.syncId)
    }
}
