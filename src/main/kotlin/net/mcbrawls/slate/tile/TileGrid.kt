package net.mcbrawls.slate.tile

data class TileGrid(
    val width: Int,
    val height: Int,
) {
    /**
     * The total size of the slate, based on the width and height.
     */
    val size: Int = width * height

    /**
     * A fixed-size array of the tiles stored in this grid.
     */
    private val tiles: Array<Tile?> = arrayOfNulls(size)

    /**
     * The last available tile slot index.
     */
    val lastIndex: Int = tiles.lastIndex

    /**
     * Whether this tile grid should be redrawn.
     */
    var dirty: Boolean = false

    /**
     * Sets a slot tile at the given index.
     */
    operator fun set(index: Int, tile: Tile) {
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
    operator fun set(x: Int, y: Int, tile: Tile): Int {
        val index = toIndex(x, y, width)
        set(index, tile)
        return index
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
        val index = toIndex(x, y)
        return this[index]
    }

    /**
     * Clears a slot tile from the given index.
     */
    fun clear(index: Int) {
        assertSlotIndex(index)
        tiles[index] = null

        // TODO on tile change
    }

    fun forEach(action: (Int, Tile?) -> Unit) {
        tiles.forEachIndexed(action)
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

    companion object {
        fun toIndex(x: Int, y: Int, width: Int = 9): Int {
            return y * width + x
        }
    }
}
