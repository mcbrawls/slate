package net.mcbrawls.slate.tile

import net.minecraft.screen.ScreenHandlerType

class HandledTileGrid(
    /**
     * The screen handler type sent to the client.
     * Affects what the client sees and how it communicates back to the server.
     */
    val screenHandlerType: ScreenHandlerType<*>,
) : TileGrid(screenHandlerType.width, screenHandlerType.height)
