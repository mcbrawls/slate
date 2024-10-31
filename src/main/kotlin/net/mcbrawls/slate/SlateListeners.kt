package net.mcbrawls.slate

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.mcbrawls.slate.screen.InventorySlateScreenHandler
import net.mcbrawls.slate.screen.slot.ClickType
import net.mcbrawls.slate.screen.slot.TileClickContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.world.World

object SlateListeners : ModInitializer, UseItemCallback {
    override fun onInitialize() {
        UseItemCallback.EVENT.register(this)
    }

    private fun interact(player: ServerPlayerEntity, hand: Hand, button: Int) {
        if (hand != Hand.MAIN_HAND) {
            return
        }

        val currentScreenHandler = player.currentScreenHandler
        if (currentScreenHandler is InventorySlateScreenHandler) {
            val selectedSlot = player.inventory.selectedSlot
            val slate = currentScreenHandler.slate

            val slotIndex = slate.hotbarStartIndex + selectedSlot + 1
            val tile = slate[slotIndex]
            val context = TileClickContext(tile, button, SlotActionType.PICKUP, ClickType.parse(button, SlotActionType.PICKUP), emptySet(), player)
            slate.onSlotClicked(context)

            currentScreenHandler.syncState()
        }
    }

    internal fun onUse(player: ServerPlayerEntity, hand: Hand) {
        interact(player, hand, 1)
    }

    internal fun onSwing(player: ServerPlayerEntity, hand: Hand) {
        interact(player, hand, 0)
    }

    override fun interact(player: PlayerEntity, world: World, hand: Hand): ActionResult {
        // cancel real inventory interactions when inventory slates are open
        if (player.currentScreenHandler is InventorySlateScreenHandler) {
            return ActionResult.FAIL
        }

        return ActionResult.PASS
    }
}
