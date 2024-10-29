package net.mcbrawls.slate

import net.mcbrawls.slate.screen.SlateScreenHandler
import net.minecraft.server.network.ServerPlayerEntity

data class HandledSlate(
    val player: ServerPlayerEntity,
    var syncId: Int,
    val screenHandler: SlateScreenHandler,
)
