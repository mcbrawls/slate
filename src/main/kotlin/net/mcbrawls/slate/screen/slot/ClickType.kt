package net.mcbrawls.slate.screen.slot

import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.slot.SlotActionType

enum class ClickType {
    LEFT,
    RIGHT,

    /**
     * Only works in creative mode.
     */
    MIDDLE,

    NUMBER_KEY,
    OFFHAND,
    THROW;

    companion object {
        /**
         * Parses a click type from the given data.
         */
        fun parse(button: Int, actionType: SlotActionType): ClickType {
            return when(actionType) {
                SlotActionType.SWAP -> if (button == PlayerInventory.OFF_HAND_SLOT) OFFHAND else NUMBER_KEY
                SlotActionType.CLONE -> MIDDLE
                SlotActionType.THROW -> THROW
                else -> if (button == 0) LEFT else RIGHT
            }
        }
    }
}
