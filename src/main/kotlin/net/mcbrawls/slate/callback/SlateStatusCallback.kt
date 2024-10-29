package net.mcbrawls.slate.callback

import net.mcbrawls.slate.Slate
import net.minecraft.server.network.ServerPlayerEntity

fun interface SlateStatusCallback : SlateCallback {
    fun onStatus(slate: Slate, player: ServerPlayerEntity)
}
