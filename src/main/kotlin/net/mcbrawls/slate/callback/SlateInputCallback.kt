package net.mcbrawls.slate.callback

import net.mcbrawls.slate.Slate
import net.minecraft.server.network.ServerPlayerEntity

fun interface SlateInputCallback {
    fun onInput(slate: Slate, player: ServerPlayerEntity, input: String)
}
