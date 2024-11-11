package net.mcbrawls.slate.tile

enum class RedirectType {
    /**
     * Redirects the entire tile.
     */
    NORMAL,

    /**
     * Redirects the tile, hiding the stack at the new location.
     */
    INVISIBLE
}
