package net.mcbrawls.slate.tile

import net.mcbrawls.slate.Slate
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

class RedirectedTile(
    val parent: Tile,
    val type: RedirectType,
) : Tile() {
    override fun createDisplayedStack(slate: Slate, player: ServerPlayerEntity): ItemStack {
        return when (type) {
            RedirectType.NORMAL -> parent.createDisplayedStack(slate, player)
            RedirectType.INVISIBLE -> ItemStack.EMPTY
        }
    }
}
