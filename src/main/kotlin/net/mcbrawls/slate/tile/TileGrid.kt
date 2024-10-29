package net.mcbrawls.slate.tile

class TileGrid(
    val width: Int,
    val height: Int,
) {
    /**
     * The total size of the slate, based on the width and height.
     */
    val size: Int = width * height

    private val tiles: Array<Tile?> = arrayOfNulls(size)

    val lastIndex: Int = tiles.lastIndex

    /**
     * Sets a slot tile at the given index.
     */
    operator fun set(index: Int, tile: Tile) {
        assertSlotIndex(index)
        tiles[index] = tile

        // TODO on tile change
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
