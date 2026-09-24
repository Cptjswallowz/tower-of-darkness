package com.towerofdarkness.app

import com.towerofdarkness.app.data.MidRunAshbrand
import com.towerofdarkness.app.data.MidRunLoadout
import com.towerofdarkness.app.data.MidRunResume
import com.towerofdarkness.app.data.MidRunSlot
import com.towerofdarkness.app.domain.path.NodeType
import com.towerofdarkness.app.domain.path.PathEdge
import com.towerofdarkness.app.domain.path.PathGenerator
import com.towerofdarkness.app.domain.path.PathNode
import com.towerofdarkness.app.domain.path.TowerPath
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.nav.GameController.Companion.FoggedTapKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * v0.1.24-rumorcharge: separate rumor vs Free Scout wallets; Scout-first fogged tap;
 * mid-run persist of both remaining charges; UI can report rumor counter at 0.
 */
class RumorChargeV0124Test {

    private fun fogPath(
        fogId: String = "fog",
        fogType: NodeType = NodeType.SHOP,
        rumor: String = "old rumor",
        otherId: String = "other",
        otherRumor: String = "other fog rumor"
    ): TowerPath {
        val nodes = listOf(
            PathNode("start", NodeType.START, 0, 1, "s", revealed = true),
            PathNode(fogId, fogType, 1, 0, rumor, revealed = false),
            PathNode(otherId, NodeType.TREASURE, 1, 1, otherRumor, revealed = false),
            PathNode("boss", NodeType.BOSS, 2, 0, "b")
        )
        val edges = listOf(
            PathEdge("start", fogId),
            PathEdge("start", otherId),
            PathEdge(fogId, "boss"),
            PathEdge(otherId, "boss")
        )
        return TowerPath(1, nodes, edges, "start")
    }

    @Test
    fun separateWallets_spendingScoutDoesNotChangeRumorRerolls() {
        val path = fogPath()
        val result = GameController.dispatchFoggedNodeTap(
            path = path,
            nodeId = "fog",
            freeScoutCharges = 1,
            rumorRerolls = 1,
            newRumor = "should not apply"
        )
        assertNotNull(result)
        assertEquals(FoggedTapKind.SCOUT, result!!.kind)
        assertEquals(0, result.freeScoutCharges)
        assertEquals(1, result.rumorRerolls) // rumor wallet untouched
        assertTrue(result.path.nodes.first { it.id == "fog" }.scoutedTypeOnly)
        assertEquals("old rumor", result.path.nodes.first { it.id == "fog" }.rumor)
    }

    @Test
    fun separateWallets_spendingRumorDoesNotChangeScoutCharges() {
        val path = fogPath()
        val result = GameController.dispatchFoggedNodeTap(
            path = path,
            nodeId = "fog",
            freeScoutCharges = 0,
            rumorRerolls = 1,
            newRumor = "fresh whisper"
        )
        assertNotNull(result)
        assertEquals(FoggedTapKind.RUMOR, result!!.kind)
        assertEquals(0, result.freeScoutCharges)
        assertEquals(0, result.rumorRerolls)
        assertFalse(result.path.nodes.first { it.id == "fog" }.scoutedTypeOnly)
        assertFalse(result.path.nodes.first { it.id == "fog" }.revealed)
        assertEquals("fresh whisper", result.path.nodes.first { it.id == "fog" }.rumor)
    }

    @Test
    fun rumorSpend_decrements1to0_changesOnlyThatNode_secondTapNoOp() {
        val path = fogPath(otherRumor = "keep me")
        val first = GameController.applyRumorReroll(path, "fog", charges = 1, newRumor = "rerolled A")
        assertNotNull(first)
        val (p1, charges1) = first!!
        assertEquals(0, charges1)
        assertEquals("rerolled A", p1.nodes.first { it.id == "fog" }.rumor)
        assertEquals("keep me", p1.nodes.first { it.id == "other" }.rumor)

        val second = GameController.applyRumorReroll(p1, "fog", charges = charges1, newRumor = "should not")
        assertNull("charges=0 must no-op", second)
        assertEquals("rerolled A", p1.nodes.first { it.id == "fog" }.rumor)

        val dispatchAtZero = GameController.dispatchFoggedNodeTap(
            p1, "fog", freeScoutCharges = 0, rumorRerolls = 0, newRumor = "nope"
        )
        assertNull(dispatchAtZero)
    }

    @Test
    fun scoutSpend_decrements_at0NoOp() {
        val path = fogPath()
        val first = GameController.applyFreeScout(path, "fog", charges = 1)
        assertNotNull(first)
        assertEquals(0, first!!.second)
        assertTrue(first.first.nodes.first { it.id == "fog" }.scoutedTypeOnly)

        assertNull(GameController.applyFreeScout(first.first, "other", charges = 0))
        assertNull(
            GameController.dispatchFoggedNodeTap(
                first.first, "other", freeScoutCharges = 0, rumorRerolls = 0, newRumor = "x"
            )
        )
    }

    @Test
    fun priority_whenBothPositive_firstTapSpendsScoutNotRumor() {
        val path = fogPath(rumor = "before")
        val result = GameController.dispatchFoggedNodeTap(
            path = path,
            nodeId = "fog",
            freeScoutCharges = 1,
            rumorRerolls = 1,
            newRumor = "must not land"
        )
        assertNotNull(result)
        assertEquals(FoggedTapKind.SCOUT, result!!.kind)
        assertEquals(0, result.freeScoutCharges)
        assertEquals(1, result.rumorRerolls)
        assertNotEquals("must not land", result.path.nodes.first { it.id == "fog" }.rumor)
        assertEquals("before", result.path.nodes.first { it.id == "fog" }.rumor)
        assertTrue(result.path.nodes.first { it.id == "fog" }.scoutedTypeOnly)

        // Second tap on another fogged node with scout=0 spends rumor
        val second = GameController.dispatchFoggedNodeTap(
            path = result.path,
            nodeId = "other",
            freeScoutCharges = result.freeScoutCharges,
            rumorRerolls = result.rumorRerolls,
            newRumor = "now rumor"
        )
        assertNotNull(second)
        assertEquals(FoggedTapKind.RUMOR, second!!.kind)
        assertEquals(0, second.rumorRerolls)
        assertEquals(0, second.freeScoutCharges)
        assertEquals("now rumor", second.path.nodes.first { it.id == "other" }.rumor)
    }

    @Test
    fun midRunSave_roundTrip_preservesBothRemainingCharges() {
        val seed = 42_024L
        val path = PathGenerator.generate(1, Random(MidRunSlot.floorSeed(seed, 1)))
        val snap = MidRunSlot.fromPath(path, "floor1_$seed")
        val original = MidRunSlot(
            runId = "run-rumorcharge",
            rngSeed = seed,
            floor = 1,
            pendingStairContinue = false,
            path = snap,
            playerHp = 22,
            playerMaxHp = 30,
            runWallet = 5,
            runRemnantsEarned = 5,
            nodesCleared = 1,
            loadout = MidRunLoadout(locked = true, cardIds = listOf("strike", "guard", "spark", "cleave", "brace")),
            ashbrand = MidRunAshbrand("ashbrand", 1, 0),
            freeScoutCharges = 0, // spent mid-floor
            rumorRerolls = 1,     // still available
            combatSpeed2x = false,
            unlocksThisRun = emptyList(),
            shopVisits = emptyList(),
            resume = MidRunResume.Path
        )
        val decoded = MidRunSlot.decode(MidRunSlot.encode(original))
        assertNotNull(decoded)
        assertEquals(0, decoded!!.freeScoutCharges)
        assertEquals(1, decoded.rumorRerolls)

        // Also verify remaining after both partially spent shapes
        val bothSpent = original.copy(freeScoutCharges = 0, rumorRerolls = 0)
        val again = MidRunSlot.decode(MidRunSlot.encode(bothSpent))!!
        assertEquals(0, again.freeScoutCharges)
        assertEquals(0, again.rumorRerolls)
    }

    @Test
    fun uiString_rumorCounterAtZero_stillReportableAsZero() {
        assertEquals("Rumor re-rolls left: 1", GameController.rumorRerollsLabel(1))
        assertEquals("Rumor re-rolls left: 0", GameController.rumorRerollsLabel(0))
        // Must not hardcode stuck-at-1
        assertFalse(GameController.rumorRerollsLabel(0).contains(": 1"))
    }

    @Test
    fun bothZero_dispatchIsNoOp() {
        val path = fogPath()
        assertNull(
            GameController.dispatchFoggedNodeTap(
                path, "fog", freeScoutCharges = 0, rumorRerolls = 0, newRumor = "x"
            )
        )
    }
}
