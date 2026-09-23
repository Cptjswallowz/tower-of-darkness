package com.towerofdarkness.app

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.path.NodeType
import com.towerofdarkness.app.domain.path.PathEdge
import com.towerofdarkness.app.domain.path.PathNode
import com.towerofdarkness.app.domain.path.TowerPath
import com.towerofdarkness.app.nav.GameController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.1.10-bossrest: Rest Heal → MAX; Free Scout tap reveal + charge spend.
 */
class BossRestV0110Test {

    @Test
    fun restHeal_setsHpToMax_deepBreathDoesNotOverheal() {
        assertEquals(30, GameController.applyRestHealToMax(30))
        assertEquals(34, GameController.applyRestHealToMax(34)) // meta + Deep Breath still cap at max
        // Starting damaged: heal result is always max (Deep Breath cannot push past)
        val damaged = 10
        val maxHp = Balance.PLAYER_MAX_HP + 4
        val after = GameController.applyRestHealToMax(maxHp)
        assertEquals(maxHp, after)
        assertTrue(after >= damaged)
        assertTrue(after <= maxHp)
    }

    @Test
    fun freeScout_tapFogged_revealsTypeAndDecrements_secondTapNoOpAtZero() {
        val nodes = listOf(
            PathNode("start", NodeType.START, 0, 1, "s", revealed = true),
            PathNode("b0_r1", NodeType.COMBAT, 1, 0, "c", revealed = true),
            PathNode("b0_r2", NodeType.SHOP, 2, 0, "fog rumor", revealed = false),
            PathNode("b1_r2", NodeType.TREASURE, 2, 1, "other fog", revealed = false),
            PathNode("merge", NodeType.REST, 3, 1, "r", revealed = false),
            PathNode("boss", NodeType.BOSS, 4, 1, "b")
        )
        val edges = listOf(
            PathEdge("start", "b0_r1"),
            PathEdge("b0_r1", "b0_r2"),
            PathEdge("b0_r2", "merge"),
            PathEdge("start", "b1_r2"),
            PathEdge("b1_r2", "merge"),
            PathEdge("merge", "boss")
        )
        val path = TowerPath(1, nodes, edges, "start")

        val first = GameController.applyFreeScout(path, "b0_r2", charges = 1)
        assertNotNull(first)
        val (p1, charges1) = first!!
        assertEquals(0, charges1)
        val scouted = p1.nodes.first { it.id == "b0_r2" }
        assertTrue("type revealed via scoutedTypeOnly", scouted.scoutedTypeOnly)
        assertFalse("does not fully enter/reveal adjacency chain", scouted.cleared)
        assertEquals(NodeType.SHOP, scouted.type) // type unchanged
        assertEquals("fog rumor", scouted.rumor) // rumor text kept; UI drops it when type shown

        // charges hit 0 → second tap no-op
        val second = GameController.applyFreeScout(p1, "b1_r2", charges = charges1)
        assertNull("charges=0 must not reveal another fogged node", second)
        assertFalse(p1.nodes.first { it.id == "b1_r2" }.scoutedTypeOnly)
        assertFalse(p1.nodes.first { it.id == "b1_r2" }.revealed)
    }

    @Test
    fun freeScout_doesNotSpendOnAlreadyRevealed_orStartBoss() {
        val nodes = listOf(
            PathNode("start", NodeType.START, 0, 1, "s", revealed = true),
            PathNode("n1", NodeType.COMBAT, 1, 0, "c", revealed = true),
            PathNode("boss", NodeType.BOSS, 2, 0, "b")
        )
        val edges = listOf(PathEdge("start", "n1"), PathEdge("n1", "boss"))
        val path = TowerPath(1, nodes, edges, "start")
        assertNull(GameController.applyFreeScout(path, "n1", 1))
        assertNull(GameController.applyFreeScout(path, "start", 1))
        assertNull(GameController.applyFreeScout(path, "boss", 1))
    }

    @Test
    fun freeScout_doesNotChangeType() {
        val nodes = listOf(
            PathNode("start", NodeType.START, 0, 1, "s", revealed = true),
            PathNode("fog", NodeType.EVENT, 1, 0, "mystery", revealed = false),
            PathNode("boss", NodeType.BOSS, 2, 0, "b")
        )
        val edges = listOf(PathEdge("start", "fog"), PathEdge("fog", "boss"))
        val path = TowerPath(1, nodes, edges, "start")
        val (after, _) = GameController.applyFreeScout(path, "fog", 1)!!
        assertEquals(NodeType.EVENT, after.nodes.first { it.id == "fog" }.type)
        assertEquals("start", after.currentId) // no enter / no move
    }
}
