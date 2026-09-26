package com.towerofdarkness.app

import com.towerofdarkness.app.domain.combat.CombatFx
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.FxRecipient
import com.towerofdarkness.app.domain.combat.FxTier
import com.towerofdarkness.app.domain.combat.WakeArt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.1.40-fxfix — no screen flash, shields back, target-only slash.
 * Aim from v0.1.38 + size/hold from v0.1.39; half-stage clip + tamed glow.
 * See docs/fxfix-v0140.md.
 */
class CombatFxFixV0140Test {

    @Test
    fun tag_isFxfixV0140() {
        assertEquals("v0.1.43-slashread", CombatFx.TAG)
    }

    @Test
    fun recipientClipRanges_areDisjointHalves() {
        val you = CombatFx.recipientClipXFrac(FxRecipient.YOU)
        val foe = CombatFx.recipientClipXFrac(FxRecipient.FOE)
        assertEquals(0f, you.start, 0.0001f)
        assertEquals(0.5f, you.endInclusive, 0.0001f)
        assertEquals(0.5f, foe.start, 0.0001f)
        assertEquals(1f, foe.endInclusive, 0.0001f)
        // Interiors disjoint (shared edge at 0.5 only)
        assertTrue(you.endInclusive <= foe.start + 0.0001f)
        assertTrue(you.start < you.endInclusive)
        assertTrue(foe.start < foe.endInclusive)
        // No interior overlap: mid of YOU is not in FOE open start
        assertTrue(0.25f in you)
        assertFalse(0.25f in foe)
        assertTrue(0.75f in foe)
        assertFalse(0.75f in you)
    }

    @Test
    fun slashGeom_stillNoSpanBothBusts() {
        for (tier in listOf(FxTier.SMALL, FxTier.MEDIUM)) {
            for (r in FxRecipient.entries) {
                val g = CombatFx.slashCutGeom(r, tier)
                assertFalse("must not span You↔foe: $r $tier", g.spansBothBusts())
                // Cut center stays inside recipient clip half
                val clip = CombatFx.recipientClipXFrac(r)
                assertTrue(
                    "center outside clip: ${g.centerXFrac} not in $clip",
                    g.centerXFrac in clip
                )
            }
        }
    }

    @Test
    fun thickness_is1_4xPrior_sizeHoldKept() {
        assertEquals(CombatFx.THICK_SMALL_PRIOR * 1.4f, CombatFx.THICK_SMALL, 0.001f)
        assertEquals(CombatFx.THICK_MEDIUM_PRIOR * 1.4f, CombatFx.THICK_MEDIUM, 0.001f)
        assertEquals(2.1f, CombatFx.THICK_SMALL, 0.001f)
        assertEquals(4.2f, CombatFx.THICK_MEDIUM, 0.001f)
        assertEquals(CombatFx.THICKNESS_READ_MULT, 1.4f, 0.001f)
        // Stroke ms +200 vs 0.1.38
        assertEquals(400L, CombatFx.STROKE_SMALL_MS)
        assertEquals(480L, CombatFx.STROKE_MEDIUM_MS)
        assertEquals(700L, WakeArt.FRAME_SLASH_MS)
        // Brace hold / fade / radius
        assertEquals(700L, CombatFx.BRACE_PIP_MS)
        assertEquals(200L, CombatFx.BRACE_PIP_FADE_MS)
        assertEquals(350L, CombatFx.fxHoldMs(CombatFx.BRACE_PIP_MS, 2))
        assertEquals(0.027f, CombatFx.BRACE_PIP_RADIUS_FRAC, 0.0001f)
        assertEquals(0.95f, CombatFx.bracePipAlpha(0f), 0.001f)
    }

    @Test
    fun glowWidth_lePriorAbsolute_coreGe1_4xPriorCore() {
        // Glow absolute = priorThickness × 7 (not current × 7)
        val glowSmall = CombatFx.strokeGlowWidth(CombatFx.THICK_SMALL)
        val glowMedium = CombatFx.strokeGlowWidth(CombatFx.THICK_MEDIUM)
        val priorGlowSmall = CombatFx.THICK_SMALL_PRIOR * CombatFx.STROKE_GLOW_PRIOR_MULT
        val priorGlowMedium = CombatFx.THICK_MEDIUM_PRIOR * CombatFx.STROKE_GLOW_PRIOR_MULT
        assertEquals(priorGlowSmall, glowSmall, 0.001f)
        assertEquals(priorGlowMedium, glowMedium, 0.001f)
        // Must not exceed 0.1.38 absolute glow
        assertTrue(glowSmall <= priorGlowSmall + 0.001f)
        assertTrue(glowMedium <= priorGlowMedium + 0.001f)
        // Unbounded bloom (thickness × 7) would be larger — assert we are below that
        assertTrue(glowSmall < CombatFx.THICK_SMALL * 7f - 0.01f)
        assertTrue(glowMedium < CombatFx.THICK_MEDIUM * 7f - 0.01f)

        val coreSmall = CombatFx.strokeCoreWidth(CombatFx.THICK_SMALL)
        val coreMedium = CombatFx.strokeCoreWidth(CombatFx.THICK_MEDIUM)
        val priorCoreSmall = CombatFx.THICK_SMALL_PRIOR * CombatFx.STROKE_CORE_MULT
        val priorCoreMedium = CombatFx.THICK_MEDIUM_PRIOR * CombatFx.STROKE_CORE_MULT
        assertEquals(priorCoreSmall * 1.4f, coreSmall, 0.001f)
        assertEquals(priorCoreMedium * 1.4f, coreMedium, 0.001f)
        assertTrue(coreSmall >= priorCoreSmall * 1.4f - 0.001f)
        assertTrue(coreMedium >= priorCoreMedium * 1.4f - 0.001f)
    }

    @Test
    fun playSpec_aimOwners_hostflintSealPulseMantleVowRustGuard() {
        // Hostflint — player Small slash on FOE
        val host = CombatFx.playSpec(CombatFx.specForPlayer("hostflint"), fxPlayer = true)
        assertEquals(FxRecipient.FOE, host.beat.recipient)
        assertEquals(FxRecipient.FOE, host.beat.stroke!!.recipient)
        assertEquals(2.1f, host.beat.stroke!!.thickness, 0.001f)
        assertEquals(0, host.bracePipCount)

        // Seal Pulse — enemy Medium slash on YOU
        val seal = CombatFx.playSpec(
            CombatFx.specForEnemy("seal_pulse", EnemyKind.DRAGON),
            fxPlayer = false
        )
        assertEquals(FxRecipient.YOU, seal.beat.recipient)
        assertEquals(FxRecipient.YOU, seal.beat.stroke!!.recipient)
        assertEquals(4.2f, seal.beat.stroke!!.thickness, 0.001f)

        // Iron Mantle — Brace pips on YOU, no slash
        val mantle = CombatFx.playSpec(
            CombatFx.specForPlayer("iron_mantle"),
            braceGained = 3,
            fxPlayer = true
        )
        assertNull(mantle.beat.stroke)
        assertEquals(3, mantle.bracePipCount)
        assertEquals(FxRecipient.YOU, mantle.braceOwner)
        assertEquals(FxRecipient.YOU, CombatFx.recipientClipXFrac(mantle.braceOwner!!).let {
            // owner YOU → left half
            assertEquals(0f, it.start, 0.0001f)
            FxRecipient.YOU
        })

        // Vow Plate — Brace on YOU
        val vow = CombatFx.playSpec(
            CombatFx.specForPlayer("vow_plate"),
            braceGained = 5,
            fxPlayer = true
        )
        assertEquals(FxRecipient.YOU, vow.braceOwner)
        assertEquals(5, vow.bracePipCount)
        assertNull(vow.beat.stroke)

        // Rust Guard — Brace on FOE
        val rust = CombatFx.playSpec(
            CombatFx.specForEnemy("rust_guard", EnemyKind.ORC),
            braceGained = 2,
            fxPlayer = false
        )
        assertEquals(FxRecipient.FOE, rust.braceOwner)
        assertEquals(2, rust.bracePipCount)
        assertNull(rust.beat.stroke)
    }
}
