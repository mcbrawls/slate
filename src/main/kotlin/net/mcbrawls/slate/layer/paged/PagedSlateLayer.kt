package net.mcbrawls.slate.layer.paged

import net.mcbrawls.slate.layer.SlateLayer
import net.mcbrawls.slate.tile.StackTile
import net.mcbrawls.slate.tile.Tile
import net.mcbrawls.slate.tile.Tile.Companion.tile
import net.mcbrawls.slate.tile.TileClickCallback
import net.minecraft.item.ItemStack

/**
 * A paginated slate layer.
 */
abstract class PagedSlateLayer(
    /**
     * The maximum slot count.
     */
    slotCount: Int,

    width: Int = 1,
    height: Int = 1,
) : SlateLayer(width, height) {
    /**
     * The maximum slot count.
     */
    var slotCount: Int = slotCount
        set(value) {
            field = value
            currentPage = currentPage
        }

    /**
     * The maximum page.
     */
    val maxPage: Int get() = (slotCount - 1) / (width * height)

    /**
     * The currently displayed page.
     */
    var currentPage: Int = 0
        set(value) {
            field = wrapPage(value, maxPage)
        }

    init {
        updateTileGrid()
    }

    /**
     * Creates a tile for the given index.
     */
    abstract fun createTile(
        /**
         * The calculated index from all tiles of all pages.
         */
        index: Int
    ): Tile?

    /**
     * Updates the tile grid to new tiles for the active page.
     */
    fun updateTileGrid() {
        tiles.clear()

        if (slotCount < 0) {
            return
        }

        val size = tiles.baseSize
        val baseIndex = currentPage * size
        (0 until size).forEach { index ->
            val calculatedIndex = baseIndex + index
            val tile = createTile(calculatedIndex)
            tiles[index] = tile
        }
    }

    fun createPageChangeTile(title: String, modifier: (Int) -> Int, stack: ItemStack, callback: TileClickCallback? = null): Tile {
        if (maxPage <= 0) {
            return StackTile.EMPTY
        }

        return tile(stack) {
            tooltip(title)

            callbacks {
                onGenericClick { slate, tile, context ->
                    currentPage = modifier.invoke(currentPage)
                    updateTileGrid()
                    callback?.onClick(slate, tile, context)
                }
            }
        }
    }

    fun createNextPageTile(stack: ItemStack, callback: TileClickCallback? = null): Tile {
        return createPageChangeTile("Next Page", { it + 1 }, stack, callback)
    }

    fun createPreviousPageTile(stack: ItemStack, callback: TileClickCallback? = null): Tile {
        return createPageChangeTile("Previous Page", { it - 1 }, stack, callback)
    }

    companion object {
        /**
         * Wraps a page around a maximum page value.
         */
        fun wrapPage(page: Int, maxPage: Int): Int {
            if (page > maxPage) {
                return 0
            }

            if (page < 0) {
                return maxPage
            }

            return page
        }
    }
}
