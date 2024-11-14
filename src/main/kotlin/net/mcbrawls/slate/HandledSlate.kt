package net.mcbrawls.slate

import net.mcbrawls.slate.screen.SlateScreenHandler
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

/**
 * A slate's handled data.
 */
data class HandledSlate<T : Slate>(
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
    val screenHandler: SlateScreenHandler<T>,
) {
    /**
     * Sends a new title for the current screen to the player.
     */
    fun sendTitle(title: Text) {
        val packet = OpenScreenS2CPacket(screenHandler.syncId, screenHandler.type, title)
        player.networkHandler.sendPacket(packet)

        screenHandler.syncState()
    }
}
