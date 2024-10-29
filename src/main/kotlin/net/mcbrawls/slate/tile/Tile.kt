package net.mcbrawls.slate.tile

import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.screen.TileSlot
import net.minecraft.item.ItemStack

/**
 * A slot within a slate.
 */
class Tile(var stack: ItemStack) {
    fun createSlot(slate: Slate, index: Int, x: Int, y: Int): TileSlot {
        return TileSlot(slate, index, x, y)
    }

    companion object {
        /**
         * Builds a default tile.
         */
        inline fun tile(stack: ItemStack, builder: Tile.() -> Unit = {}): Tile {
            return Tile(stack).apply(builder)
        }
    }
}
