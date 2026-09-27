package com.towerofdarkness.app.domain.sound

/**
 * v0.1.51-ironclash — curated one-shot roles → asset paths + same-frame duck rule.
 * Presentation only; no combat math.
 */
object IronclashSfx {
    const val TAG = "v0.1.51-ironclash"

    const val KEY_IMPACT = "impact"
    const val KEY_LEGENDARY = "legendary"
    const val KEY_EMBER = "ember"
    const val KEY_BRACE = "brace"
    const val KEY_SOFTEN = "soften"
    const val KEY_UI = "ui"
    const val KEY_DICE = "dice"
    const val KEY_MISS = "miss"
    /** Internal layer under legendary sting (Wake clash ≤400ms). */
    const val KEY_WAKE_CLASH = "wake_clash"

    const val FILE_IMPACT = "ironclash/IRONCLASH_23_Flesh_Hit_Light_05.ogg"
    const val FILE_WAKE_CLASH = "ironclash/IRONCLASH_03_Sword_Clash_04_wake380.ogg"
    const val FILE_BRACE = "ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg"
    const val FILE_EMBER = "lentikula/lentikula_fire_impact_5_short220.ogg"
    const val FILE_SOFTEN = "lentikula/lentikula_heal_impact_4_soften.ogg"
    const val FILE_UI = "kenney_ui/click_001.ogg"
    const val FILE_DICE = "sfx_dice.wav"
    const val FILE_STING = "sfx_legendary_sting.wav"
    const val FILE_MISS = "sfx_miss.wav"

    /** Runtime asset paths under app/src/main/assets/ (canonical). */
    const val PATH_IMPACT = "audio/$FILE_IMPACT"
    const val PATH_WAKE_CLASH = "audio/$FILE_WAKE_CLASH"
    const val PATH_BRACE = "audio/$FILE_BRACE"
    const val PATH_EMBER = "audio/$FILE_EMBER"
    const val PATH_SOFTEN = "audio/$FILE_SOFTEN"
    const val PATH_UI = "audio/$FILE_UI"
    const val PATH_DICE = "audio/$FILE_DICE"
    const val PATH_STING = "audio/$FILE_STING"
    const val PATH_MISS = "audio/$FILE_MISS"

    /** Role → filename (under assets/sfx/ curated source / audio/ runtime). */
    val ROLE_TO_FILENAME: Map<String, String> = mapOf(
        KEY_IMPACT to FILE_IMPACT.substringAfter('/'),
        "wake" to FILE_WAKE_CLASH.substringAfter('/'),
        KEY_BRACE to FILE_BRACE.substringAfter('/'),
        KEY_EMBER to FILE_EMBER.substringAfter('/'),
        KEY_SOFTEN to FILE_SOFTEN.substringAfter('/'),
        KEY_UI to FILE_UI.substringAfter('/'),
        KEY_DICE to FILE_DICE,
        "sting" to FILE_STING,
        KEY_MISS to FILE_MISS
    )

    /** SoundPool load map: play key → asset path. */
    val LOAD_MAP: Map<String, String> = mapOf(
        KEY_IMPACT to PATH_IMPACT,
        KEY_WAKE_CLASH to PATH_WAKE_CLASH,
        KEY_BRACE to PATH_BRACE,
        KEY_EMBER to PATH_EMBER,
        KEY_SOFTEN to PATH_SOFTEN,
        KEY_UI to PATH_UI,
        KEY_DICE to PATH_DICE,
        KEY_LEGENDARY to PATH_STING,
        KEY_MISS to PATH_MISS
    )

    /** Garnish keys that duck when Impact (or legendary) shares the frame. */
    val GARNISH_KEYS: Set<String> = setOf(KEY_EMBER, KEY_SOFTEN)

    const val VOL_FULL = 0.80f
    const val VOL_SOFTEN_SOLO = 0.62f
    const val VOL_GARNISH_DUCK = 0.35f
    const val VOL_WAKE_CLASH = 0.70f

    /**
     * Same-frame rule: Impact (and legendary layers) play full;
     * garnish (ember/soften) duck to ~0.35 when Impact or legendary is present.
     * Soften alone plays ~0.55–0.7. Empty / blank keys dropped.
     */
    fun resolveFrame(sounds: List<String>): List<Pair<String, Float>> {
        val keys = sounds.map { it.trim() }.filter { it.isNotEmpty() }
        if (keys.isEmpty()) return emptyList()
        val hasImpact = keys.any { it == KEY_IMPACT }
        val hasLegendary = keys.any { it == KEY_LEGENDARY }
        val duckGarnish = hasImpact || hasLegendary
        val out = mutableListOf<Pair<String, Float>>()
        val seen = mutableSetOf<String>()
        for (key in keys) {
            if (!seen.add(key)) continue
            when (key) {
                KEY_LEGENDARY -> {
                    out += KEY_LEGENDARY to VOL_FULL
                    out += KEY_WAKE_CLASH to VOL_WAKE_CLASH
                }
                KEY_SOFTEN -> {
                    val vol = if (duckGarnish) VOL_GARNISH_DUCK else VOL_SOFTEN_SOLO
                    out += KEY_SOFTEN to vol
                }
                KEY_EMBER -> {
                    val vol = if (duckGarnish) VOL_GARNISH_DUCK else VOL_FULL
                    out += KEY_EMBER to vol
                }
                else -> out += key to VOL_FULL
            }
        }
        return out
    }
}
