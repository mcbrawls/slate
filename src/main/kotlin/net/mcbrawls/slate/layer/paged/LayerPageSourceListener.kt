package net.mcbrawls.slate.layer.paged

/**
 * Listens to modifications to the internal collection and updates the layer accordingly.
 */
class LayerPageSourceListener<T>(
    collection: MutableCollection<T>,
) {
    private val _collection: MutableCollection<T> = collection

    val collection: Collection<T> get() = _collection.toList()

    /**
     * Modify and update the internal collection.
     */
    fun modify(layer: PagedSlateLayer, action: MutableCollection<T>.() -> Unit) {
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
        fun <T> MutableCollection<T>.layerPageListener() = LayerPageSourceListener(this)
    }
}
