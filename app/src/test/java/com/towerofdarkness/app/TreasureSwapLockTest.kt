package com.towerofdarkness.app

import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.nav.GameController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * v0.1.6-swaplock: Gain is rolled once per treasure visit; preview taps / cancel
 * must not reroll it.
 */
class TreasureSwapLockTest {

    @Test
    fun treasureVisit_twoPreviews_sameGainId() {
        val loadoutIds = CardCatalog.defaultLoadoutIds
        val unlocked = CardCatalog.starterUnlockedIds()
        val rng = Random(42)

        var visit = GameController.treasureVisitEnter(loadoutIds.toSet(), unlocked, rng)
        assertNotNull("visit must roll a Gain", visit.gainId)
        val lockedGain = visit.gainId!!

        visit = visit.beginPreview(loadoutIds[0], loadoutIds, rng)
        assertEquals(lockedGain, visit.gainId)
        assertEquals(loadoutIds[0], visit.loseId)

        visit = visit.cancelPreview()
        assertEquals(lockedGain, visit.gainId)
        assertNull(visit.loseId)

        visit = visit.beginPreview(loadoutIds[1], loadoutIds, rng)
        assertEquals(
            "second preview in same visit must keep the same gainId",
            lockedGain,
            visit.gainId
        )
        assertEquals(loadoutIds[1], visit.loseId)
    }

    @Test
    fun treasureVisit_cancelDoesNotRerollGain() {
        val loadoutIds = CardCatalog.defaultLoadoutIds
        val unlocked = CardCatalog.starterUnlockedIds()
        // Many seeds: enter → preview → cancel → preview keeps gain
        for (seed in 0..31) {
            val rng = Random(seed.toLong())
            var visit = GameController.treasureVisitEnter(loadoutIds.toSet(), unlocked, rng)
            val g = visit.gainId ?: continue
            visit = visit.beginPreview(loadoutIds[0], loadoutIds, rng)
            visit = visit.cancelPreview()
            visit = visit.beginPreview(null, loadoutIds, rng)
            assertEquals("seed=$seed", g, visit.gainId)
            assertTrue(visit.loseId in loadoutIds)
        }
    }

    @Test
    fun pickTreasureGainId_neverInLoadout() {
        val loadoutIds = CardCatalog.defaultLoadoutIds.toSet()
        val unlocked = CardCatalog.starterUnlockedIds()
        repeat(50) { i ->
            val id = GameController.pickTreasureGainId(loadoutIds, unlocked, Random(i.toLong()))
            assertNotNull(id)
            assertTrue("$id must not be in loadout", id!! !in loadoutIds)
        }
    }
}
