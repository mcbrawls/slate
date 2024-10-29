package net.mcbrawls.slate.test

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.mcbrawls.slate.Slate.Companion.slate
import net.mcbrawls.slate.SlatePlayer
import net.mcbrawls.slate.tile.Tile.Companion.tile
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.ActionResult

class SlateTest : ModInitializer {
    override fun onInitialize() {
        UseItemCallback.EVENT.register { player, _, hand ->
            val stack = player.getStackInHand(hand)

            if (stack.isOf(Items.STICK)) {
                val slatePlayer = player as SlatePlayer
                slatePlayer.openSlate(
                    slate {
                        tiles[0, 0] = tile(ItemStack(Items.STONE))
                    }
                )
                ActionResult.SUCCESS_SERVER
            } else {
                ActionResult.PASS
            }
        }
    }
}
