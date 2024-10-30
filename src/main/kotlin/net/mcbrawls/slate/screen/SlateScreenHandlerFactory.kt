package net.mcbrawls.slate.screen

import net.mcbrawls.slate.Slate
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerFactory
import net.minecraft.text.Text

class SlateScreenHandlerFactory(
    val slate: Slate,
    val factory: ScreenHandlerFactory
) : NamedScreenHandlerFactory {
    override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity): ScreenHandler? {
        return factory.createMenu(syncId, playerInventory, player)
    }

    override fun getDisplayName(): Text {
        return slate.title
    }

    companion object {
        /**
         * Creates a custom screen handler factory out of the given slate.
         */
        fun create(slate: Slate): SlateScreenHandlerFactory {
            return SlateScreenHandlerFactory(slate) { syncId, _, player -> SlateScreenHandler(slate, player, slate.screenHandlerType, syncId) }
        }
    }
}
