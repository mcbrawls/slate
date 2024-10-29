package net.mcbrawls.slate.screen

import net.mcbrawls.slate.Slate
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType

class SlateScreenHandler(
    val slate: Slate,
    type: ScreenHandlerType<*>,
    syncId: Int
) : ScreenHandler(type, syncId) {
    init {
        setupSlots()
    }

    private fun setupSlots() {
        val tileGrid = slate.tileGrid
        tileGrid.forEach { index, tile ->
            if (tile != null) {
                val slot = tile.createSlot(slate, index, 0, 0)
                addSlot(slot)
            }
        }

        val gridSize = tileGrid.size
        for (x in 0 until 9) {
            for (y in 0 .. 4) {
                val slot = TileSlot(slate, y * 9 + x + gridSize, 0, 0)
                addSlot(slot)
            }
        }
    }

    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return true
    }
}
