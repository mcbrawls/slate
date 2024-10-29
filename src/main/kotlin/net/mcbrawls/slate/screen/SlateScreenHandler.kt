package net.mcbrawls.slate.screen

import net.mcbrawls.slate.Slate
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity

class SlateScreenHandler(
    val slate: Slate,
    type: ScreenHandlerType<*>,
    syncId: Int
) : ScreenHandler(type, syncId) {
    init {
        setupSlots()
    }

    private fun setupSlots() {
        val tileGrid = slate.tiles
        tileGrid.forEach { index, tile ->
            if (tile != null) {
                val slot = tile.createSlot(slate, index, 0, 0)
                addSlot(slot)
            }
        }

        val gridSize = tileGrid.size
        for (x in 0 until 9) {
            for (y in 0 .. 4) {
                val slot = TileSlot(slate, y * 9 + x + gridSize, 0, 0)
                addSlot(slot)
            }
        }
    }

    fun setSlot(index: Int, slot: Slot) {
        slots[index] = slot
    }

    override fun sendContentUpdates() {
        super.sendContentUpdates()
        slate.tick()
    }

    override fun onClosed(player: PlayerEntity) {
        if (player is ServerPlayerEntity) {
            slate.onClosed(player)
        }

        cursorStack = ItemStack.EMPTY
    }

    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun internalOnSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType, player: PlayerEntity) {
        if (actionType != SlotActionType.PICKUP_ALL) {
            println("$slotIndex, $button, $actionType, $player")
        }
    }

    override fun canInsertIntoSlot(slot: Slot): Boolean {
        return false
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return true
    }
}
