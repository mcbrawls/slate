package net.mcbrawls.slate.tile

import net.mcbrawls.slate.MinecraftUnit
import net.mcbrawls.slate.screen.slot.ClickType
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
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
     * The base item stack to be displayed.
     */
    abstract fun createBaseStack(player: ServerPlayerEntity): ItemStack

    /**
     * Creates the final displayed stack for this tile.
     */
    fun createDisplayedStack(player: ServerPlayerEntity): ItemStack {
        val stack = createBaseStack(player)

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
                    text.fillStyle(Style.EMPTY.withFormatting(Formatting.RESET))
                }

                stack.set(
                    DataComponentTypes.LORE,
                    LoreComponent(tooltip.toList())
                )
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
