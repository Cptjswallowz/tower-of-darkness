package com.towerofdarkness.app.domain.climb

import kotlin.random.Random

/**
 * F3 floor rumors — v0.1.54-trollkept PART A.
 * Exactly two sticky lines; do NOT spend rumorRerolls / freeScoutCharges.
 * See docs/trollkept-v0154.md.
 */
object FloorRumors {
    /** First-ever F3 explainer (exact lock). */
    const val F3_EXPLAINER =
        "From this floor on, read the rumors, pick five + weapon, then lock until the next stair."

    val TROLL_POOL: List<String> = listOf(
        "A clubber in the dark hides behind stone before it swings.",
        "Something down here stacks Brace, then brings a club.",
        "The caves keep a thick one. Club first, Hide if you let it live."
    )

    val PATH_POOL: List<String> = listOf(
        "Two fights stand before the last rest.",
        "A stall or a cache is still open on this floor.",
        "The halls favor orcs more than goblins."
    )

    /**
     * Even pick 1 from troll pool (slot 0) + 1 from path pool (slot 1).
     * Returns exactly two strings. Does not touch wallets.
     */
    fun generate(rng: Random): List<String> {
        val troll = TROLL_POOL[rng.nextInt(TROLL_POOL.size)]
        val path = PATH_POOL[rng.nextInt(PATH_POOL.size)]
        return listOf(troll, path)
    }

    /** True when [lines] is a valid sticky pair (size 2, non-blank). */
    fun isValidPair(lines: List<String>): Boolean =
        lines.size == 2 && lines.all { it.isNotBlank() }
}
