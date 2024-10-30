package net.mcbrawls.slate.screen.slot

import net.minecraft.screen.slot.SlotActionType

enum class ClickModifier {
    SHIFT,
    DOUBLE;

    companion object {
        /**
         * Parses click modifiers from the given data.
         */
        fun parse(actionType: SlotActionType): List<ClickModifier> {
            return buildList {
                if (actionType == SlotActionType.QUICK_MOVE) {
                    add(SHIFT)
                }

                if (actionType == SlotActionType.PICKUP_ALL) {
                    add(DOUBLE)
                }
            }
        }
    }
}
