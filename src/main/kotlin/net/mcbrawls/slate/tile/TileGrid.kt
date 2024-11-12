package net.mcbrawls.slate.tile

import net.minecraft.screen.ScreenHandlerType

open class TileGrid(val width: Int, val height: Int) {
    /**
     * The size of the slate WITHOUT the player's inventory.
     */
    val baseSize: Int = width * height

    /**
     * A fixed-size array of all tiles stored in this grid.
     */
    open val tiles: Array<Tile?> = arrayOfNulls(baseSize)

    /**
     * Redirects a tile index to another tile index.
     */
    val redirects: MutableMap<Int, Pair<Int, RedirectType>> = mutableMapOf()

    /**
     * The total size of all tiles.
     */
    val size: Int get() = tiles.size

    /**
     * The last available tile slot index.
     */
    val lastIndex: Int get() = tiles.lastIndex

    /**
     * The start of the hotbar slot indexes.
     */
    val hotbarStartIndex: Int get() = lastIndex - 8

    /**
     * Sets a slot tile at the given index.
     */
    operator fun set(index: Int, tile: Tile?): Boolean {
        if (checkSlotIndex(index)) {
            if (tile != tiles[index]) {
                tiles[index] = tile
                return true
            }
        }

        return false
    }

    /**
     * Sets a slot tile from the given coordinates.
     * @return the calculated index
     */
    operator fun set(x: Int, y: Int, tile: Tile?): Int {
        val index = toIndex(x, y, width)
        set(index, tile)
        return index
    }

    /**
     * Sets a slot tile within the player's hotbar.
     * @return the calculated index
     */
    fun setHotbar(index: Int, tile: Tile?): Int {
        val hotbarIndex = hotbarStartIndex + index
        set(hotbarIndex, tile)
        return hotbarIndex
    }

    /**
     * Gets a slot tile from the given index.
     */
    operator fun get(index: Int): Tile? {
        redirects[index]?.also { (trueIndex, type) ->
            tiles.getOrNull(trueIndex)?.also { tile ->
                return RedirectedTile(tile, type)
            }
        }

        return tiles.getOrNull(index)
    }

    /**
     * Gets a slot tile from the given coordinates.
     */
    operator fun get(x: Int, y: Int): Tile? {
        val index = toIndex(x, y, width)
        return this[index]
    }

    /**
     * Gets a slot tile from the hotbar.
     */
    fun getHotbar(index: Int): Tile? {
        val hotbarIndex = hotbarStartIndex + index
        return this[hotbarIndex]
    }

    /**
     * Clears a slot tile from the given index.
     */
    fun clear(index: Int): Boolean {
        return set(index, null)
    }

    /**
     * Clears all tile slots.
     */
    fun clear() {
        return tiles.fill(null)
    }

    /**
     * Sets a redirect on this tile grid.
     */
    fun redirect(index: Int, otherIndex: Int, type: RedirectType = RedirectType.NORMAL) {
        redirects[index] = otherIndex to type
    }

    fun forEach(action: (Int, Tile?) -> Unit) {
        tiles.toList().forEachIndexed(action)
    }

    /**
     * Verifies that an index is within the slot size bounds.
     */
    private fun checkSlotIndex(index: Int): Boolean {
        return index <= lastIndex
    }

    companion object {
        /**
         * Converts coordinates to a tile grid index.
         */
        fun toIndex(x: Int, y: Int, width: Int): Int {
            return y * width + x
        }

        /**
         * Creates a tile grid from the dimensions of the given screen handler type.
         */
        fun create(type: ScreenHandlerType<*>): HandledTileGrid {
            return HandledTileGrid(type)
        }

        /**
         * The width of this screen handler type.
         */
        val ScreenHandlerType<*>.width: Int get() {
            return when (this) {
                ScreenHandlerType.CRAFTING -> 2
                ScreenHandlerType.SMITHING -> 4
                ScreenHandlerType.GENERIC_3X3 -> 3
                ScreenHandlerType.HOPPER -> 5
                ScreenHandlerType.BREWING_STAND -> 1
                ScreenHandlerType.ENCHANTMENT -> 1
                ScreenHandlerType.STONECUTTER -> 1
                ScreenHandlerType.BEACON -> 1
                ScreenHandlerType.BLAST_FURNACE -> 1
                ScreenHandlerType.FURNACE -> 1
                ScreenHandlerType.SMOKER -> 1
                ScreenHandlerType.ANVIL -> 1
                ScreenHandlerType.GRINDSTONE -> 1
                ScreenHandlerType.MERCHANT -> 1
                ScreenHandlerType.CARTOGRAPHY_TABLE -> 1
                ScreenHandlerType.LOOM -> 1
                else -> 9
            }
        }

        /**
         * The height of this screen handler type.
         */
        val ScreenHandlerType<*>.height: Int get() {
            return when (this) {
                ScreenHandlerType.GENERIC_9X6 -> 6
                ScreenHandlerType.CRAFTING -> 6
                ScreenHandlerType.GENERIC_9X5 -> 5
                ScreenHandlerType.GENERIC_9X4 -> 4
                ScreenHandlerType.GENERIC_9X3 -> 3
                ScreenHandlerType.GENERIC_9X2 -> 2
                ScreenHandlerType.ENCHANTMENT -> 2
                ScreenHandlerType.STONECUTTER -> 2
                ScreenHandlerType.GENERIC_9X1 -> 1
                ScreenHandlerType.BEACON -> 1
                ScreenHandlerType.HOPPER -> 1
                ScreenHandlerType.BREWING_STAND -> 1
                ScreenHandlerType.SMITHING -> 1
                else -> 3
            }
        }
    }
}
