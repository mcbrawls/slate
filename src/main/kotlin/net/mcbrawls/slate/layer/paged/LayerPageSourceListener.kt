package net.mcbrawls.slate.layer.paged

/**
 * Listens to modifications to the internal collection and updates the layer accordingly.
 */
class LayerPageSourceListener<T>(
    val layer: PagedSlateLayer,
    private val _collection: MutableCollection<T> = mutableListOf(),
) {
    val collection: Collection<T> get() = _collection.toList()

    /**
     * Modify and update the internal collection.
     */
    fun modify(action: MutableCollection<T>.() -> Unit) {
        synchronized(_collection) {
            action.invoke(_collection)
            layer.slotCount = _collection.size
            layer.updateTileGrid()
        }
    }

    companion object {
        /**
         * Creates a page source listener from this collection.
         */
        fun <T> MutableCollection<T>.layerPageListener(layer: PagedSlateLayer) = LayerPageSourceListener(layer, this)
    }
}
