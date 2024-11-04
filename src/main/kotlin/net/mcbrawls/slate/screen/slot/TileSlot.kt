package net.mcbrawls.slate.screen.slot

import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.screen.SlateInventory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import java.util.Optional

class TileSlot(
    val slate: Slate,
    tileIndex: Int,
    x: Int,
    y: Int
) : Slot(SlateInventory, tileIndex, x, y) {
    override fun getStack(): ItemStack {
        val tile = slate[index]
        return tile?.createDisplayedStack() ?: ItemStack.EMPTY
    }

    override fun takeStack(amount: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun canTakeItems(player: PlayerEntity): Boolean {
        return false
    }

    override fun canTakePartial(player: PlayerEntity): Boolean {
        return false
    }

    override fun insertStack(stack: ItemStack, count: Int): ItemStack {
        return stack
    }

    override fun insertStack(stack: ItemStack): ItemStack {
        return ItemStack.EMPTY
    }

    override fun tryTakeStackRange(min: Int, max: Int, player: PlayerEntity): Optional<ItemStack> {
        return Optional.empty()
    }

    override fun setStackNoCallbacks(stack: ItemStack) {
        super.setStackNoCallbacks(stack)
    }

    override fun setStack(stack: ItemStack) {
    }

    override fun hasStack(): Boolean {
        return true
    }

    override fun canInsert(stack: ItemStack): Boolean {
        return false
    }

    override fun markDirty() {
    }
}
