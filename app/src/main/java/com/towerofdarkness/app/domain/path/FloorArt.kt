package com.towerofdarkness.app.domain.path

/**
 * Path map floor art — v0.1.31-floorart.
 * Pure mapping for unit tests (no Compose).
 * Backdrop behind PathScreen; type tokens replace letter glyphs when Art crops ship.
 * Aligns with docs/floorart-v0131.md + docs/art-audio/FLOOR_ART_v0.1.31.md.
 */
object FloorArt {
    const val TAG = "v0.1.31-floorart"

    /** Shared Floor 1 / Floor 2 path backdrop (drawable basename). Art-dimmed JPG. */
    const val BACKDROP_DRAWABLE = "floor_backdrop"

    /** Floor 3 cave backdrop — darker teal/coal tint; F1/F2 keep [BACKDROP_DRAWABLE]. */
    const val BACKDROP_CAVE_DRAWABLE = "floor_backdrop_cave"

    /** Node type token basenames — Art crops (`node_*.png`, 128×128 RGBA). */
    const val TOKEN_START = "node_start"
    const val TOKEN_COMBAT = "node_combat"
    const val TOKEN_TREASURE = "node_treasure"
    const val TOKEN_SHOP = "node_shop"
    const val TOKEN_REST = "node_rest"
    const val TOKEN_FOG = "node_fog"

    /**
     * Backdrop Image alpha before scrim — plate is already Art-dimmed;
     * keep a light compose alpha so gold/purple rings still read.
     */
    const val BACKDROP_ALPHA = 0.72f

    /** Dark scrim over backdrop (Floor 1). */
    const val SCRIM_ALPHA_FLOOR1 = 0.28f

    /**
     * Extra black overlay on Floor 2 (~10–15% darker than Floor 1).
     * Combined with [SCRIM_ALPHA_FLOOR1] via layered scrim.
     */
    const val FLOOR2_EXTRA_DARK_ALPHA = 0.12f

    /**
     * Extra black overlay on Floor 3 (darker cave than Floor 2).
     * Layered on top of Floor 1 scrim + Floor 2 extra when floor >= 3.
     */
    const val FLOOR3_EXTRA_DARK_ALPHA = 0.14f

    /** Token Image size inside the existing 52dp node circle. */
    const val TOKEN_SIZE_DP = 40

    /** Expected Art token set (Boss keeps letter treatment — not on node sheet). */
    fun expectedTokenNames(): Set<String> = setOf(
        TOKEN_START,
        TOKEN_COMBAT,
        TOKEN_TREASURE,
        TOKEN_SHOP,
        TOKEN_REST,
        TOKEN_FOG
    )

    /**
     * Drawable basename for a path node chip icon.
     * Fogged (type hidden) → [TOKEN_FOG].
     * Revealed / scouted / Start / current → real type token.
     * [NodeType.EVENT] revealed → [TOKEN_FOG] (? motif; no separate Event crop).
     * [NodeType.BOSS] → null (keep existing letter "B"; Art sheet has no boss crop).
     */
    fun tokenDrawableName(type: NodeType, showType: Boolean): String? {
        if (!showType) return TOKEN_FOG
        return when (type) {
            NodeType.START -> TOKEN_START
            NodeType.COMBAT -> TOKEN_COMBAT
            NodeType.TREASURE -> TOKEN_TREASURE
            NodeType.SHOP -> TOKEN_SHOP
            NodeType.REST -> TOKEN_REST
            NodeType.EVENT -> TOKEN_FOG
            NodeType.BOSS -> null
        }
    }

    /** Floor 2+ uses F2 extra darkening over the shared backdrop. */
    fun floor2Darker(floor: Int): Boolean = floor >= 2

    /** Floor 3+ uses Art cave plate ([BACKDROP_CAVE_DRAWABLE]); extra scrim still applies lightly. */
    fun floor3Darker(floor: Int): Boolean = floor >= 3

    fun backdropDrawableName(floor: Int): String =
        if (floor >= 3) BACKDROP_CAVE_DRAWABLE else BACKDROP_DRAWABLE

    fun scrimAlpha(floor: Int): Float {
        var a = SCRIM_ALPHA_FLOOR1
        if (floor2Darker(floor) && floor < 3) a += FLOOR2_EXTRA_DARK_ALPHA
        // F3 plate is already Art-darkened; keep base scrim only (no stacked F2/F3 extras)
        return a
    }

    /**
     * Art drop detection. Override for tests; otherwise known set on disk under drawable/.
     */
    @Volatile
    var tokenArtPresentOverride: Set<String>? = null

    fun tokenArtShipped(drawableName: String): Boolean {
        tokenArtPresentOverride?.let { return drawableName in it }
        if (drawableName in expectedTokenNames() && allTokenFilesPresent()) return true
        return tokenDrawableFileExists(drawableName)
    }

    fun allTokensShipped(): Boolean =
        expectedTokenNames().all { tokenArtShipped(it) }

    fun anyTokenShipped(): Boolean =
        expectedTokenNames().any { tokenArtShipped(it) }

    fun backdropShipped(): Boolean = backdropFileExists(BACKDROP_DRAWABLE)

    fun caveBackdropShipped(): Boolean = backdropFileExists(BACKDROP_CAVE_DRAWABLE)

    private fun backdropFileExists(drawableName: String): Boolean {
        val candidates = listOf(
            "app/src/main/res/drawable/$drawableName.jpg",
            "app/src/main/res/drawable/$drawableName.png",
            "app/src/main/res/drawable/$drawableName.webp",
            "src/main/res/drawable/$drawableName.jpg",
            "src/main/res/drawable/$drawableName.png",
            "src/main/res/drawable/$drawableName.webp"
        )
        return candidates.any { java.io.File(it).isFile }
    }

    private fun allTokenFilesPresent(): Boolean =
        expectedTokenNames().all { tokenDrawableFileExists(it) }

    private fun tokenDrawableFileExists(drawableName: String): Boolean {
        val candidates = listOf(
            "app/src/main/res/drawable/$drawableName.png",
            "app/src/main/res/drawable/$drawableName.jpg",
            "app/src/main/res/drawable/$drawableName.webp",
            "src/main/res/drawable/$drawableName.png",
            "src/main/res/drawable/$drawableName.jpg",
            "src/main/res/drawable/$drawableName.webp"
        )
        return candidates.any { java.io.File(it).isFile }
    }
}
