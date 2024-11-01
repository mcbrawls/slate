package net.mcbrawls.slate.tile

import net.minecraft.screen.ScreenHandlerType

data class TileGrid(
    /**
     * The screen handler type sent to the client.
     * Affects what the client sees and how it communicates back to the server.
     */
    val screenHandlerType: ScreenHandlerType<*>,

    val width: Int,
    val height: Int,
) {
    /**
     * The size of the slate WITHOUT the player's inventory.
     */
    val baseSize: Int = width * height

    /**
     * A fixed-size array of all tiles stored in this grid.
     */
    private val tiles: Array<Tile?> = arrayOfNulls(baseSize + INVENTORY_SIZE)

    /**
     * The total size of all tiles.
     */
    val size: Int = tiles.size

    /**
     * The last available tile slot index.
     */
    val lastIndex: Int = tiles.lastIndex

    /**
     * The start of the hotbar slot indexes.
     */
    val hotbarStartIndex: Int get() = tiles.lastIndex - 8

    /**
     * Whether this tile grid should be redrawn.
     */
    var dirty: Boolean = false

    /**
     * Sets a slot tile at the given index.
     */
    operator fun set(index: Int, tile: Tile?) {
        assertSlotIndex(index)

        if (tile != tiles[index]) {
            tiles[index] = tile
            dirty = true
        }
    }

    /**
     * Sets a slot tile from the given coordinates.
     * @return the calculated index
     */
    operator fun set(x: Int, y: Int, tile: Tile?): Int {
        assertWidthMatchesInventory()
        val index = toIndex(x, y, width)
        set(index, tile)
        return index
    }

    /**
     * Sets a slot tile from the given coordinates, within the player inventory space.
     * @return the calculated index
     */
    fun setInventory(x: Int, y: Int, tile: Tile?): Int {
        val index = toIndex(x, y, INVENTORY_WIDTH) + baseSize
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
        return tiles.getOrNull(index)
    }

    /**
     * Gets a slot tile from the given coordinates.
     */
    operator fun get(x: Int, y: Int): Tile? {
        assertWidthMatchesInventory()
        val index = toIndex(x, y, width)
        return this[index]
    }

    /**
     * Gets a slot tile from the given coordinates.
     */
    fun getInventory(x: Int, y: Int): Tile? {
        val index = toIndex(x, y, INVENTORY_WIDTH) + baseSize
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
    fun clear(index: Int) {
        assertSlotIndex(index)
        tiles[index] = null
    }

    fun forEach(action: (Int, Tile?) -> Unit) {
        tiles.toList().forEachIndexed(action)
    }

    /**
     * Asserts an index is within the slot size bounds.
     * @throws IllegalArgumentException if the index if out of bounds
     */
    private fun assertSlotIndex(index: Int) {
        if (index > lastIndex) {
            throw IllegalArgumentException("Tile placed out of bounds: index $index, size $size")
        }
    }

    /**
     * Asserts that this tile grid's width matches the default player inventory width.
     * Used to prevent usage of coordinates in non-matching tile grids.
     *
     * @throws IllegalStateException if the width does not match that of the player inventory
     */
    private fun assertWidthMatchesInventory() {
        if (width != INVENTORY_WIDTH) {
            throw IllegalStateException("Width is not consistent throughout tile grid")
        }
    }

    companion object {
        const val INVENTORY_WIDTH = 9
        const val INVENTORY_HEIGHT = 4
        const val INVENTORY_SIZE = INVENTORY_WIDTH * INVENTORY_HEIGHT

        /**
         * Converts coordinates to a tile grid index.
         */
        fun toIndex(x: Int, y: Int, width: Int): Int {
            return y * width + x
        }

        /**
         * Creates a tile grid from the dimensions of the given screen handler type.
         */
        fun create(type: ScreenHandlerType<*>): TileGrid {
            return TileGrid(type, type.width, type.height)
        }

        /**
         * The width of this screen handler type.
         */
        val ScreenHandlerType<*>.width: Int get() {
            return when(this) {
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
            return when(this) {
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
