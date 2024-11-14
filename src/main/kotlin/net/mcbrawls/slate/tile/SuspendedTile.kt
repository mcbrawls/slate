package net.mcbrawls.slate.tile

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.mcbrawls.slate.Slate
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Util
import org.apache.commons.lang3.mutable.MutableLong

/**
 * A tile provided by a suspended function.
 */
class SuspendedTile(
    /**
     * The tile to display when no tile has been calculated.
     */
    val baseTile: Tile,

    /**
     * The suspended function factory.
     */
    val tileFactory: ChildFactory,
) : Tile() {
    /**
     * The created tile.
     */
    private var tile: Tile? = null
        set(value) {
            field = value
            value?.also(::setMetadataFrom)
        }

    private var latestCallTimestamp = MutableLong(0L)
    private var factoryState: FactoryState = FactoryState.EMPTY

    fun updateTile(slate: Slate, player: ServerPlayerEntity): Tile {
        if (factoryState == FactoryState.EMPTY) {
            factoryState = FactoryState.SUSPENDED

            val timestamp = Util.getMeasuringTimeMs()
            latestCallTimestamp.value = timestamp

            GlobalScope.async {
                val newTile = tileFactory.create(slate, player)

                // only update for latest function call
                synchronized(latestCallTimestamp) {
                    if (timestamp == latestCallTimestamp.value) {
                        tile = newTile
                        factoryState = FactoryState.FINISHED
                    }
                }
            }
        }

        return tile ?: baseTile
    }

    fun refreshTile() {
        factoryState = FactoryState.EMPTY
    }

    override fun createDisplayedStack(slate: Slate, player: ServerPlayerEntity): ItemStack {
        val trueTile = updateTile(slate, player)
        return trueTile.createDisplayedStack(slate, player)
    }

    fun interface ChildFactory {
        suspend fun create(slate: Slate, player: ServerPlayerEntity): Tile?
    }

    enum class FactoryState {
        EMPTY,
        SUSPENDED,
        FINISHED
    }

    companion object {
        fun tile(baseTile: Tile, tileFactory: ChildFactory): SuspendedTile {
            return SuspendedTile(baseTile, tileFactory)
        }
    }
}
