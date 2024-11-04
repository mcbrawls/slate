package net.mcbrawls.slate.screen

import net.mcbrawls.slate.InventorySlate
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity

class InventorySlateScreenHandler(
    slate: InventorySlate,
    player: ServerPlayerEntity,
    type: ScreenHandlerType<*>
) : SlateScreenHandler<InventorySlate>(slate, player, type, SYNC_ID) {
    companion object {
        const val SYNC_ID = 0
    }
}
