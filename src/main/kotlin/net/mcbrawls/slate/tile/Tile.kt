package net.mcbrawls.slate.tile

import net.mcbrawls.slate.MinecraftUnit
import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.screen.slot.ClickModifier
import net.mcbrawls.slate.screen.slot.ClickType
import net.mcbrawls.slate.tooltip.TooltipChunk
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

/**
 * A slot within a slate.
 */
abstract class Tile {
    /**
     * The complete tooltip of the tile stack.
     * The first element is the name, and the rest is flushed to the tooltip.
     * All are formatted as reset by default, not vanilla's purple color.
     */
    val tooltip: MutableList<Text> = mutableListOf()

    /**
     * Whether this tile can be picked up and moved by the client.
     */
    var immovable: Boolean = true

    private val clickCallbacks: MutableList<Pair<ClickType, TileClickCallback>> = mutableListOf()

    /**
     * Adds tooltips to this tile.
     */
    @JvmName("tooltipText")
    fun tooltip(tooltips: Collection<Text>) {
        tooltip.addAll(tooltips)
    }

    /**
     * Adds tooltips to this tile.
     */
    fun tooltip(vararg tooltips: Text) {
        tooltip(tooltips.toList())
    }

    /**
     * Adds tooltips to this tile.
     */
    @JvmName("tooltipString")
    fun tooltip(tooltips: Collection<String>) {
        tooltip.addAll(tooltips.map(Text::literal))
    }

    /**
     * Adds tooltips to this tile.
     */
    fun tooltip(vararg tooltips: String) {
        tooltip(tooltips.toList())
    }

    /**
     * Adds tooltip chunks to this tile.
     */
    @JvmName("tooltipTooltipChunk")
    fun tooltip(chunks: List<TooltipChunk>) {
        val lastIndex = chunks.lastIndex
        chunks.forEachIndexed { index, chunk ->
            // append chunk
            chunk.texts.forEach { text ->
                tooltip.add(text.copy().fillStyle(chunk.style))
            }

            // append break
            if (index != lastIndex) {
                tooltip.add(Text.empty())
            }
        }
    }

    /**
     * Adds tooltip chunks to this tile.
     */
    fun tooltip(vararg tooltips: TooltipChunk) {
        tooltip(tooltips.toList())
    }

    /**
     * Adds a click callback for the given click type.
     */
    fun onClick(clickType: ClickType = ClickType.LEFT, callback: TileClickCallback) {
        clickCallbacks.add(clickType to callback)
    }

    /**
     * Adds a click callback for the given click type, using left click in both screen contexts.
     */
    fun onGenericClick(callback: TileClickCallback) {
        onClick(ClickType.LEFT) { slate, tile, context ->
            if (context.withinScreen && !context.modifiers.contains(ClickModifier.DOUBLE)) {
                callback.onClick(slate, tile, context)
            }
        }

        onClick(ClickType.RIGHT) { slate, tile, context ->
            if (!context.withinScreen && !context.modifiers.contains(ClickModifier.DOUBLE)) {
                callback.onClick(slate, tile, context)
            }
        }
    }

    /**
     * Combines all callbacks for the given click type into one callable object.
     */
    fun collectClickCallbacks(clickType: ClickType): TileClickCallback {
        return TileClickCallback { slate, tile, context ->
            clickCallbacks
                .filter { it.first == clickType }
                .map { it.second }
                .forEach { callback -> callback.onClick(slate, tile, context) }
        }
    }

    /**
     * The base item stack to be displayed.
     */
    abstract fun createBaseStack(slate: Slate, player: ServerPlayerEntity): ItemStack

    /**
     * Creates the final displayed stack for this tile.
     */
    open fun createDisplayedStack(slate: Slate, player: ServerPlayerEntity): ItemStack {
        val stack = createBaseStack(slate, player)

        addTooltip(stack)
        addImmovable(stack)

        return stack
    }

    internal fun addTooltip(stack: ItemStack) {
        if (tooltip.isEmpty()) {
            stack.set(DataComponentTypes.HIDE_TOOLTIP, MinecraftUnit.INSTANCE)
        } else {
            val tooltip = tooltip.map(Text::copy).toMutableList()
            val name = tooltip.removeFirst()

            stack.set(DataComponentTypes.ITEM_NAME, name)

            if (tooltip.isNotEmpty()) {
                tooltip.forEach { text ->
                    text.styled { style ->
                        var finalStyle = style

                        if (finalStyle.color == null) {
                            finalStyle = finalStyle.withColor(Formatting.WHITE)
                        }

                        if (finalStyle.italic == null) {
                            finalStyle = finalStyle.withItalic(false)
                        }

                        finalStyle
                    }
                }

                stack.set(DataComponentTypes.LORE, LoreComponent(tooltip.toList()))
            }
        }
    }

    internal fun addImmovable(stack: ItemStack) {
        if (immovable) {
            val nbt = NbtCompound()

            val bukkitNbt = NbtCompound()
            bukkitNbt.putBoolean(IMMOVABLE_TAG, true)

            nbt.put(BUKKIT_COMPOUND_ID, bukkitNbt)

            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt))
        }
    }

    override fun toString(): String {
        return "Tile"
    }

    companion object {
        const val BUKKIT_COMPOUND_ID = "PublicBukkitValues"
        const val NOXESIUM_NAMESPACE = "noxesium"

        val IMMOVABLE_TAG: String = Identifier.of(NOXESIUM_NAMESPACE, "immovable").toString()

        /**
         * Builds a defaulted tile with an item stack.
         */
        inline fun tile(stack: ItemStack = ItemStack.EMPTY, builder: StackTile.() -> Unit = {}): StackTile {
            return tile({ StackTile(stack) }, builder)
        }

        /**
         * Builds a defaulted tile with an item.
         */
        inline fun tile(item: Item, builder: StackTile.() -> Unit = {}): StackTile {
            return tile(ItemStack(item), builder)
        }

        /**
         * Builds a tile.
         */
        inline fun <T : Tile> tile(factory: () -> T, builder: T.() -> Unit = {}): T {
            return factory.invoke().apply(builder)
        }
    }
}
