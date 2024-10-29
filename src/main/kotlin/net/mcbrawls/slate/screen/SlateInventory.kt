package net.mcbrawls.slate.screen

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

object SlateInventory : Inventory {
    override fun clear() {
    }

    override fun size(): Int {
        return 0
    }

    override fun isEmpty(): Boolean {
        return true
    }

    override fun getStack(slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun removeStack(slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun setStack(slot: Int, stack: ItemStack?) {
    }

    override fun markDirty() {
    }

    override fun canPlayerUse(player: PlayerEntity): Boolean {
        return false
    }
}
