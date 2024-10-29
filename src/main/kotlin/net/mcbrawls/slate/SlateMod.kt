package net.mcbrawls.slate

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object SlateMod : ModInitializer {
    private val logger = LoggerFactory.getLogger("slate")

    override fun onInitialize() {
        logger.info("Hello Fabric world!")
    }
}
