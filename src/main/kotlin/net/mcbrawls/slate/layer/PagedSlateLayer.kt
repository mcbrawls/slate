package net.mcbrawls.slate.layer

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

            // update page
            val oldPage = currentPage
            currentPage = oldPage
            if (currentPage == oldPage) {
                updateTileGrid()
            }
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
            val page = wrapPage(value, maxPage)

            if (field == page) {
                return
            }

            field = page
            updateTileGrid()

            // callbackHandler.collectCallbacks<SlateLayerPageChangeCallback>().invoke()
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
                onClick { slate, tile, context ->
                    callback?.onClick(slate, tile, context)
                    currentPage = modifier.invoke(currentPage)
                }
            }
        }
    }

    fun createNextPageTile(stack: ItemStack, callback: TileClickCallback? = null): Tile {
        return createPageChangeTile("Next Page", Int::unaryPlus, stack, callback)
    }

    fun createPreviousPageTile(stack: ItemStack, callback: TileClickCallback? = null): Tile {
        return createPageChangeTile("Previous Page", Int::unaryMinus, stack, callback)
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
