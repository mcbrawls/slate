package net.mcbrawls.slate.tile

import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.screen.slot.TileClickContext

fun interface TileClickCallback {
    fun onClick(slate: Slate, tile: Tile, context: TileClickContext)
}
