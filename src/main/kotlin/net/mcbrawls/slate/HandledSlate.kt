package net.mcbrawls.slate

import net.mcbrawls.slate.screen.SlateScreenHandler
import net.minecraft.server.network.ServerPlayerEntity

/**
 * A slate's handled data.
 */
data class HandledSlate(
    /**
     * The player who is viewing the slate.
     */
    val player: ServerPlayerEntity,

    /**
     * The sync id of the handler.
     */
    var syncId: Int,

    /**
     * The handler for the slate screen.
     */
    val screenHandler: SlateScreenHandler,
)
