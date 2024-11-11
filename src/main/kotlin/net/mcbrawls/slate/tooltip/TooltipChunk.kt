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
    val texts: List<Text>
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

    companion object {
        /**
         * Builds a tooltip chunk.
         */
        @JvmName("tooltipChunkText")
        inline fun tooltipChunk(texts: List<Text>, builder: TooltipChunk.() -> Unit = {}): TooltipChunk {
            return TooltipChunk(texts).apply(builder)
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
        inline fun tooltipChunk(texts: List<String>, builder: TooltipChunk.() -> Unit = {}): TooltipChunk {
            return TooltipChunk(texts.map(Text::literal)).apply(builder)
        }

        /**
         * Builds a tooltip chunk.
         */
        inline fun tooltipChunk(vararg texts: String, builder: TooltipChunk.() -> Unit = {}): TooltipChunk {
            return tooltipChunk(texts.toList(), builder)
        }
    }
}
