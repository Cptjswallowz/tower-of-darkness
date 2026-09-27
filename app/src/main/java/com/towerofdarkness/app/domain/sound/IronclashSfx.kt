package com.towerofdarkness.app.domain.sound

/**
 * v0.1.53-wakeblade — Wake heavy swing+impact under sting (unmapped from Clash/Critical).
 * Presentation only; no combat math. Keeps 1.52 slash layer volumes.
 */
object IronclashSfx {
    const val TAG = "v0.1.53-wakeblade"

    const val KEY_SWING = "slash_swing"
    const val KEY_IMPACT = "impact"
    const val KEY_LEGENDARY = "legendary"
    const val KEY_EMBER = "ember"
    const val KEY_BRACE = "brace"
    const val KEY_SOFTEN = "soften"
    const val KEY_UI = "ui"
    const val KEY_DICE = "dice"
    const val KEY_MISS = "miss"
    /** Wake swing layer — leads wake_impact by SWING_LEAD_MS. */
    const val KEY_WAKE_SWING = "wake_swing"
    /** Wake impact bed — fires with sting (lead) at SWING_LEAD_MS. */
    const val KEY_WAKE_IMPACT = "wake_impact"

    const val FILE_SWING = "ironclash/IRONCLASH_01_Sword_Swing_Light_09_plus3db.ogg"
    const val FILE_IMPACT = "ironclash/IRONCLASH_23_Flesh_Hit_Light_08.ogg"
    const val FILE_WAKE_SWING = "ironclash/IRONCLASH_02_Sword_Swing_Heavy_04_wake_plus3db.ogg"
    const val FILE_WAKE_IMPACT = "ironclash/IRONCLASH_24_Flesh_Hit_Heavy_04_wake_plus3db.ogg"
    const val FILE_BRACE = "ironclash/IRONCLASH_14_Shield_Block_Metal_02_brace350.ogg"
    const val FILE_EMBER = "lentikula/lentikula_fire_impact_5_short220.ogg"
    const val FILE_SOFTEN = "lentikula/lentikula_heal_impact_4_soften.ogg"
    const val FILE_UI = "kenney_ui/click_001.ogg"
    const val FILE_DICE = "sfx_dice.wav"
    const val FILE_STING = "sfx_legendary_sting.wav"
    const val FILE_MISS = "sfx_miss.wav"

    /** Runtime asset paths under app/src/main/assets/ (canonical). */
    const val PATH_SWING = "audio/$FILE_SWING"
    const val PATH_IMPACT = "audio/$FILE_IMPACT"
    const val PATH_WAKE_SWING = "audio/$FILE_WAKE_SWING"
    const val PATH_WAKE_IMPACT = "audio/$FILE_WAKE_IMPACT"
    const val PATH_BRACE = "audio/$FILE_BRACE"
    const val PATH_EMBER = "audio/$FILE_EMBER"
    const val PATH_SOFTEN = "audio/$FILE_SOFTEN"
    const val PATH_UI = "audio/$FILE_UI"
    const val PATH_DICE = "audio/$FILE_DICE"
    const val PATH_STING = "audio/$FILE_STING"
    const val PATH_MISS = "audio/$FILE_MISS"

    /**
     * Swing starts this many ms before impact (band 40–80; mid-band default).
     * Shared by normal slash and Wake. SoundPool has no built-in delay — SoundBus schedules via Handler.
     */
    const val SWING_LEAD_MS = 60L

    /** Role → filename (under assets/sfx/ curated source / audio/ runtime). */
    val ROLE_TO_FILENAME: Map<String, String> = mapOf(
        KEY_SWING to FILE_SWING.substringAfter('/'),
        KEY_IMPACT to FILE_IMPACT.substringAfter('/'),
        "slash_impact" to FILE_IMPACT.substringAfter('/'),
        "wake" to FILE_WAKE_IMPACT.substringAfter('/'),
        KEY_WAKE_SWING to FILE_WAKE_SWING.substringAfter('/'),
        KEY_WAKE_IMPACT to FILE_WAKE_IMPACT.substringAfter('/'),
        KEY_BRACE to FILE_BRACE.substringAfter('/'),
        KEY_EMBER to FILE_EMBER.substringAfter('/'),
        KEY_SOFTEN to FILE_SOFTEN.substringAfter('/'),
        KEY_UI to FILE_UI.substringAfter('/'),
        KEY_DICE to FILE_DICE,
        "sting" to FILE_STING,
        KEY_MISS to FILE_MISS
    )

    /** SoundPool load map: play key → asset path. No Clash/Critical for Wake. */
    val LOAD_MAP: Map<String, String> = mapOf(
        KEY_SWING to PATH_SWING,
        KEY_IMPACT to PATH_IMPACT,
        KEY_WAKE_SWING to PATH_WAKE_SWING,
        KEY_WAKE_IMPACT to PATH_WAKE_IMPACT,
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

    /** Normal slash + Wake layers — Wake loudness via +3dB baked files, not extra gain. */
    const val VOL_FULL = 0.80f
    const val VOL_SOFTEN_SOLO = 0.62f
    const val VOL_GARNISH_DUCK = 0.35f

    data class ResolvedClip(
        val key: String,
        val volume: Float,
        val delayMs: Long = 0L
    )

    /**
     * Same-frame rule: Impact expands to swing (t=0) + impact (t=SWING_LEAD_MS);
     * garnish (ember/soften) duck to ~0.35 when Impact or legendary is present.
     * Legendary expands to wake_swing @ t=0 + (sting lead + wake_impact bed) @ t=SWING_LEAD_MS.
     * Soften alone ~0.55–0.7. Empty / blank keys dropped.
     * No Clash / Parry / Block / Critical on Wake path. No extra mixer gain on Wake (+3dB baked).
     */
    fun resolveFrame(sounds: List<String>): List<ResolvedClip> {
        val keys = sounds.map { it.trim() }.filter { it.isNotEmpty() }
        if (keys.isEmpty()) return emptyList()
        val hasImpact = keys.any { it == KEY_IMPACT || it == KEY_SWING || it == "slash_impact" }
        val hasLegendary = keys.any { it == KEY_LEGENDARY }
        val duckGarnish = hasImpact || hasLegendary
        val out = mutableListOf<ResolvedClip>()
        val seen = mutableSetOf<String>()
        for (key in keys) {
            val normalized = when (key) {
                "slash_impact" -> KEY_IMPACT
                else -> key
            }
            if (!seen.add(normalized)) continue
            when (normalized) {
                KEY_IMPACT, KEY_SWING -> {
                    // Emit once even if both slash_swing and impact appear in the list.
                    if (out.none { it.key == KEY_SWING }) {
                        out += ResolvedClip(KEY_SWING, VOL_FULL, 0L)
                        out += ResolvedClip(KEY_IMPACT, VOL_FULL, SWING_LEAD_MS)
                    }
                }
                KEY_LEGENDARY -> {
                    // wake_swing leads; sting (lead) + wake_impact (bed) together after lead.
                    out += ResolvedClip(KEY_WAKE_SWING, VOL_FULL, 0L)
                    out += ResolvedClip(KEY_LEGENDARY, VOL_FULL, SWING_LEAD_MS)
                    out += ResolvedClip(KEY_WAKE_IMPACT, VOL_FULL, SWING_LEAD_MS)
                }
                KEY_SOFTEN -> {
                    val vol = if (duckGarnish) VOL_GARNISH_DUCK else VOL_SOFTEN_SOLO
                    out += ResolvedClip(KEY_SOFTEN, vol, 0L)
                }
                KEY_EMBER -> {
                    val vol = if (duckGarnish) VOL_GARNISH_DUCK else VOL_FULL
                    out += ResolvedClip(KEY_EMBER, vol, 0L)
                }
                else -> out += ResolvedClip(normalized, VOL_FULL, 0L)
            }
        }
        return out
    }
}
