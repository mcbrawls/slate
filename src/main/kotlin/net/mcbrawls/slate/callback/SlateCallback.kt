package net.mcbrawls.slate.callback

import net.mcbrawls.slate.Slate
import net.minecraft.server.network.ServerPlayerEntity

fun interface SlateCallback {
    operator fun invoke(slate: Slate, player: ServerPlayerEntity)
}
