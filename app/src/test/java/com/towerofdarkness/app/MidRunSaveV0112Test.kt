package com.towerofdarkness.app

import com.towerofdarkness.app.data.MidRunAshbrand
import com.towerofdarkness.app.data.MidRunLoadout
import com.towerofdarkness.app.data.MidRunResume
import com.towerofdarkness.app.data.MidRunShopVisit
import com.towerofdarkness.app.data.MidRunSlot
import com.towerofdarkness.app.domain.path.PathGenerator
import com.towerofdarkness.app.nav.NavState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * v0.1.12-save: mid-run slot codec, resume targets, F1 stair dual-write fields.
 * No Android / DataStore — pure serialization + resume rules.
 */
class MidRunSaveV0112Test {

    private fun sampleSlot(
        floor: Int = 2,
        pendingStair: Boolean = false,
        resume: MidRunResume = MidRunResume.Path,
        hp: Int = 17,
        wallet: Int = 14,
        weaponLevel: Int = 2,
        clearedExtra: Boolean = true
    ): MidRunSlot {
        val seed = 42_001L
        val path = PathGenerator.generate(floor, Random(MidRunSlot.floorSeed(seed, floor)))
        var snap = MidRunSlot.fromPath(path, "floor${floor}_$seed")
        if (clearedExtra && snap.nodes.size > 2) {
            // Mark first mid node cleared to simulate progress
            val mid = snap.nodes.first { it.type != "START" && it.type != "BOSS" }
            snap = snap.copy(
                cleared = snap.cleared + mid.id,
                nodes = snap.nodes.map {
                    if (it.id == mid.id) it.copy(cleared = true, revealed = true) else it
                }
            )
        }
        return MidRunSlot(
            runId = "run-test-1",
            rngSeed = seed,
            floor = floor,
            pendingStairContinue = pendingStair,
            path = snap.copy(floor = floor),
            playerHp = hp,
            playerMaxHp = 30,
            runWallet = wallet,
            runRemnantsEarned = wallet + 6,
            nodesCleared = 3,
            loadout = MidRunLoadout(
                locked = true,
                cardIds = listOf("strike", "guard", "spark", "cleave", "brace")
            ),
            ashbrand = MidRunAshbrand("ashbrand", weaponLevel, 0),
            freeScoutCharges = 1,
            rumorRerolls = 0,
            combatSpeed2x = true,
            unlocksThisRun = listOf("shadow_latch"),
            shopVisits = listOf(MidRunShopVisit("b0_r1", floor, listOf("heal_small_0"))),
            resume = resume
        )
    }

    @Test
    fun roundTrip_preservesFloor2HpRemLoadoutAshbrandClearedAndRngSeed() {
        val original = sampleSlot(floor = 2, hp = 17, wallet = 14, weaponLevel = 2)
        val json = MidRunSlot.encode(original)
        val decoded = MidRunSlot.decode(json)
        assertNotNull(decoded)
        val s = decoded!!
        assertEquals(MidRunSlot.SCHEMA, s.schema)
        assertEquals(42_001L, s.rngSeed)
        assertEquals(2, s.floor)
        assertEquals(17, s.playerHp)
        assertEquals(14, s.runWallet)
        assertEquals(true, s.loadout.locked)
        assertEquals(listOf("strike", "guard", "spark", "cleave", "brace"), s.loadout.cardIds)
        assertEquals(2, s.ashbrand.level)
        assertEquals(0, s.ashbrand.charge)
        assertEquals(true, s.combatSpeed2x)
        assertEquals(1, s.freeScoutCharges)
        assertTrue(s.path.cleared.isNotEmpty())
        assertEquals(original.path.nodes.size, s.path.nodes.size)
        val tower = MidRunSlot.toTowerPath(s.path)
        assertEquals(2, tower.floor)
        assertEquals(s.path.cursor, tower.currentId)
        assertEquals(s.path.cleared.toSet(), tower.nodes.filter { it.cleared }.map { it.id }.toSet())
    }

    @Test
    fun pendingStair_resumeIsFloorBreak_notPath() {
        val slot = sampleSlot(
            floor = 1,
            pendingStair = true,
            resume = MidRunResume.FloorBreak
        )
        assertEquals(NavState.FloorBreak, MidRunSlot.resumeNav(slot))
        // Encode/decode keeps the flag
        val again = MidRunSlot.decode(MidRunSlot.encode(slot))!!
        assertTrue(again.pendingStairContinue)
        assertEquals(MidRunResume.FloorBreak, again.resume)
        assertEquals(NavState.FloorBreak, MidRunSlot.resumeNav(again))
    }

    @Test
    fun stairContinuePayload_floor2_clearsPending_resumesPath() {
        // Dual write #2 shape: after stair Continue
        val slot = sampleSlot(floor = 2, pendingStair = false, resume = MidRunResume.Path)
        assertFalse(slot.pendingStairContinue)
        assertEquals(NavState.Path, MidRunSlot.resumeNav(slot))
        assertEquals(2, slot.floor)
        assertEquals(2, slot.path.floor)
    }

    @Test
    fun killWardenThenForceStop_onStair_resumesFloorBreak_notF1Path() {
        val afterWarden = sampleSlot(
            floor = 1,
            pendingStair = true,
            resume = MidRunResume.FloorBreak,
            hp = 12,
            wallet = 20,
            weaponLevel = 2
        )
        // Cold start / Continue must not land on F1 Path with a stair CTA
        assertEquals(NavState.FloorBreak, MidRunSlot.resumeNav(afterWarden))
        assertEquals(1, afterWarden.floor)
        assertTrue(afterWarden.pendingStairContinue)
    }

    @Test
    fun corruptOrWrongSchema_decodeReturnsNull() {
        assertNull(MidRunSlot.decode("{not json"))
        assertNull(MidRunSlot.decode("""{"schema":"v0.0.0","rng_seed":1}"""))
        assertNull(MidRunSlot.decode("""{"schema":"v0.1.12"}""")) // missing rng_seed
    }

    @Test
    fun rngSeed_requiredAndRoundTrips() {
        val slot = sampleSlot()
        val json = MidRunSlot.encode(slot)
        assertTrue("rng_seed must be present in payload", json.contains("\"rng_seed\":"))
        assertEquals(slot.rngSeed, MidRunSlot.decode(json)!!.rngSeed)
        // floorSeed is stable
        assertEquals(
            MidRunSlot.floorSeed(99L, 2),
            MidRunSlot.floorSeed(99L, 2)
        )
    }

    @Test
    fun newClimbWipe_meansNoActiveSlot() {
        // Model: after confirm wipe, slot is null → Menu hides Continue
        val wiped: MidRunSlot? = null
        assertFalse(MidRunSlot.isActiveClimb(wiped))
        val active = sampleSlot()
        assertTrue(MidRunSlot.isActiveClimb(active))
    }

    @Test
    fun noCombatSnapshotFields_inEncodedPayload() {
        val json = MidRunSlot.encode(sampleSlot())
        assertFalse(json.contains("enemy_hp"))
        assertFalse(json.contains("\"round\""))
        assertFalse(json.contains("dice"))
        assertFalse(json.contains("wake"))
    }
}
