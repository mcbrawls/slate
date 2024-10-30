package net.mcbrawls.slate.screen

import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.screen.slot.ClickModifier
import net.mcbrawls.slate.screen.slot.ClickType
import net.mcbrawls.slate.screen.slot.TileClickContext
import net.mcbrawls.slate.screen.slot.TileSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity

class SlateScreenHandler(
    val slate: Slate,
    val player: PlayerEntity,
    type: ScreenHandlerType<*>,
    syncId: Int,
) : ScreenHandler(type, syncId) {
    init {
        drawSlots()
    }

    private fun drawSlots() {
        val tileGrid = slate.tiles
        val gridSize = tileGrid.size

        // send gui slots
        for (tileIndex in 0 until gridSize) {
            val tile = tileGrid[tileIndex]
            if (tile != null) {
                val slot = tile.createSlot(slate, tileIndex, 0, 0)
                addSlot(slot)
            } else {
                addSlot(TileSlot(slate, tileIndex, 0, 0))
            }
        }
    }

    fun onAnvilInput(input: String) {
        if (player is ServerPlayerEntity) {
            slate.onAnvilInput(player, input)
        }
    }

    override fun sendContentUpdates() {
        super.sendContentUpdates()

        if (player is ServerPlayerEntity) {
            slate.onTick(player)
        }
    }

    override fun onClosed(player: PlayerEntity) {
        if (player is ServerPlayerEntity) {
            slate.onClosed(player)

            cursorStack = ItemStack.EMPTY
            sendInventory(player)
        }
    }

    override fun internalOnSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType, player: PlayerEntity) {
        val isOffhandSwap = actionType == SlotActionType.SWAP && button == PlayerInventory.OFF_HAND_SLOT
        if (isOffhandSwap) {
            clearOffhandSlotClient()
        }

        if (player is ServerPlayerEntity) {
            val modifiers = ClickModifier.parse(actionType)
            val clickType = ClickType.parse(button, actionType)
            val tile = slate.tiles[slotIndex]
            val context = TileClickContext(tile, button, actionType, clickType, modifiers, player)
            slate.onSlotClicked(context)
        }
    }

    override fun syncState() {
        super.syncState()

        // manually update offhand
        clearOffhandSlotClient()
    }

    /**
     * Updates the current offhand slot value on the client.
     */
    fun clearOffhandSlotClient() {
        val player = slate.player ?: return
        val packet = ScreenHandlerSlotUpdateS2CPacket(0, revision, PlayerInventory.OFF_HAND_SLOT + 5, ItemStack.EMPTY)
        player.networkHandler.sendPacket(packet)
    }

    /**
     * Updates the player's full inventory on the client.
     */
    fun sendInventory(player: ServerPlayerEntity) {
        val playerScreenHandler = player.playerScreenHandler
        val inventoryPacket = InventoryS2CPacket(playerScreenHandler.syncId, playerScreenHandler.nextRevision(), playerScreenHandler.stacks, playerScreenHandler.cursorStack)
        player.networkHandler.sendPacket(inventoryPacket)
    }

    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun canInsertIntoSlot(slot: Slot): Boolean {
        return false
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return true
    }
}
