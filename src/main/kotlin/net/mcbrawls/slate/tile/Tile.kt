package net.mcbrawls.slate.tile

import net.mcbrawls.slate.MinecraftUnit
import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.screen.slot.ClickType
import net.mcbrawls.slate.screen.slot.TileSlot
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.item.ItemStack
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

/**
 * A slot within a slate.
 */
open class Tile(var stack: ItemStack) {
    /**
     * The complete tooltip of the tile stack.
     * The first element is the name, and the rest is flushed to the tooltip.
     * All are formatted as reset by default, not vanilla's purple color.
     */
    val tooltip: MutableList<Text> = mutableListOf()

    private val clickCallbacks: MutableList<Pair<ClickType, TileClickCallback>> = mutableListOf()

    /**
     * Adds tooltips to this tile.
     */
    fun tooltip(vararg tooltips: Text) {
        tooltip.addAll(tooltips)
    }

    /**
     * Adds a click callback for the given click type.
     */
    fun onClick(clickType: ClickType = ClickType.LEFT, callback: TileClickCallback) {
        clickCallbacks.add(clickType to callback)
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
     * Creates the screen slot for this tile.
     */
    fun createSlot(slate: Slate, tileIndex: Int, x: Int, y: Int): TileSlot {
        return TileSlot(slate, tileIndex, x, y)
    }

    /**
     * Creates the final displayed stack for this tile.
     */
    fun getDisplayedStack(): ItemStack {
        val stack = stack.copy()

        // add tooltip components
        if (tooltip.isEmpty()) {
            stack.set(DataComponentTypes.HIDE_TOOLTIP, MinecraftUnit.INSTANCE)
        } else {
            val tooltip = tooltip.map(Text::copy).toMutableList()
            val name = tooltip.removeFirst()

            stack.set(DataComponentTypes.ITEM_NAME, name)

            if (tooltip.isNotEmpty()) {
                tooltip.forEach { text ->
                    text.fillStyle(Style.EMPTY.withFormatting(Formatting.RESET))
                }

                stack.set(
                    DataComponentTypes.LORE,
                    LoreComponent(tooltip.toList())
                )
            }
        }

        return stack
    }

    override fun toString(): String {
        val stackStr = stack.toString()
        return "Tile{$stackStr}"
    }

    companion object {
        /**
         * Builds a default tile.
         */
        inline fun tile(stack: ItemStack = ItemStack.EMPTY, builder: Tile.() -> Unit = {}): Tile {
            return Tile(stack).apply(builder)
        }
    }
}
