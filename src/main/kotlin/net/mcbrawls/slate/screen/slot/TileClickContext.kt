package net.mcbrawls.slate.screen.slot

import net.mcbrawls.slate.tile.Tile
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity

data class TileClickContext(
    /**
     * The clicked slot.
     */
    val tile: Tile?,

    /**
     * The vanilla button id.
     */
    val button: Int,

    /**
     * The vanilla action type.
     */
    val actionType: SlotActionType,

    /**
     * The interpreted base button click type.
     */
    val clickType: ClickType,

    /**
     * The interpreted modifiers of the click.
     */
    val modifiers: Collection<ClickModifier>,

    /**
     * The player who clicked the slot.
     */
    val player: ServerPlayerEntity,
)
