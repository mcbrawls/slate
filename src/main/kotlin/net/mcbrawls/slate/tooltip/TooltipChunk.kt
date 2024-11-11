package net.mcbrawls.slate.tooltip

import net.minecraft.text.Style
import net.minecraft.text.Text

/**
 * A chunk of tooltip texts, separated by a line break.
 */
class TooltipChunk(
    /**
     * The texts to be displayed as part of this chunk.
     */
    val texts: MutableList<Text> = mutableListOf()
) {
    /**
     * A style displayed over all texts in this chunk.
     */
    var style: Style = Style.EMPTY

    /**
     * Modifies the style of this tooltip chunk.
     */
    inline fun styled(styler: Style.() -> Style) {
        style = styler.invoke(style)
    }

    /**
     * Adds a text component to this chunk.
     */
    fun add(text: Text) {
        texts.add(text)
    }

    /**
     * Adds a text component to this chunk.
     */
    fun add(text: String) {
        add(Text.literal(text))
    }

    internal fun modifyTooltip(tooltip: MutableList<Text>, index: Int, lastIndex: Int) {
        if (texts.isNotEmpty()) {
            // append chunk
            texts.forEach { text ->
                tooltip.add(text.copy().fillStyle(style))
            }

            // append break
            if (index != lastIndex) {
                tooltip.add(Text.empty())
            }
        }
    }

    companion object {
        /**
         * Builds a tooltip chunk.
         */
        inline fun tooltipChunk(builder: TooltipChunk.() -> Unit = {}): TooltipChunk {
            return TooltipChunk().apply(builder)
        }

        /**
         * Builds a tooltip chunk.
         */
        @JvmName("tooltipChunkText")
        inline fun tooltipChunk(texts: Collection<Text>, builder: TooltipChunk.() -> Unit = {}): TooltipChunk {
            return TooltipChunk(texts.toMutableList()).apply(builder)
        }

        /**
         * Builds a tooltip chunk.
         */
        inline fun tooltipChunk(vararg texts: Text, builder: TooltipChunk.() -> Unit = {}): TooltipChunk {
            return tooltipChunk(texts.toList(), builder)
        }

        /**
         * Builds a tooltip chunk.
         */
        @JvmName("tooltipChunkString")
        inline fun tooltipChunk(texts: Collection<String>, builder: TooltipChunk.() -> Unit = {}): TooltipChunk {
            return tooltipChunk(texts.map(Text::literal), builder)
        }

        /**
         * Builds a tooltip chunk.
         */
        inline fun tooltipChunk(vararg texts: String, builder: TooltipChunk.() -> Unit = {}): TooltipChunk {
            return tooltipChunk(texts.toList(), builder)
        }
    }
}
