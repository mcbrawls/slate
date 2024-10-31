package net.mcbrawls.slate.screen

import net.mcbrawls.slate.InventorySlate
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.ScreenHandlerType

class InventorySlateScreenHandler(
    slate: InventorySlate,
    player: PlayerEntity,
    type: ScreenHandlerType<*>
) : SlateScreenHandler<InventorySlate>(slate, player, type, SYNC_ID) {
    companion object {
        const val SYNC_ID = 0
    }
}
