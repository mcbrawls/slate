package net.mcbrawls.slate.test

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.mcbrawls.slate.SlatePlayer
import net.minecraft.item.Items
import net.minecraft.util.ActionResult

class SlateTest : ModInitializer {
    override fun onInitialize() {
        UseItemCallback.EVENT.register { player, _, hand ->
            val stack = player.getStackInHand(hand)

            if (stack.isOf(Items.STICK)) {
                val slatePlayer = player as SlatePlayer
                slatePlayer.openSlate(TestSlate())
                ActionResult.SUCCESS_SERVER
            } else {
                ActionResult.PASS
            }
        }
    }
}
