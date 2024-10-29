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

    /**
     * Sets a slot tile at the given index.
     */
    operator fun set(index: Int, tile: Tile) {
        assertSlotIndex(index)
        tiles[index] = tile

        // TODO on tile change
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
        if (index > tiles.lastIndex) {
            throw IllegalArgumentException("Tile placed out of bounds: index $index, size $size")
        }
    }
}
