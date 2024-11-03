package net.mcbrawls.slate

import net.mcbrawls.slate.tile.TileGrid

/**
 * An instance of a layer on a slate.
 */
data class SlateLayer(
    val width: Int,
    val height: Int,
) {
    val tiles: TileGrid = TileGrid(width, height)
}
