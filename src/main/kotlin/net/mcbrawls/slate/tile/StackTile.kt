package net.mcbrawls.slate.tile

import net.mcbrawls.slate.Slate
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

class StackTile(var stack: ItemStack) : Tile() {
    constructor(item: Item) : this(ItemStack(item))

    override fun createBaseStack(slate: Slate, player: ServerPlayerEntity): ItemStack {
        return stack
    }

    override fun toString(): String {
        val stackStr = stack.toString()
        return "StackTile{$stackStr}"
    }
}
