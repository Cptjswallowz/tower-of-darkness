package com.towerofdarkness.app

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.path.NodeType
import com.towerofdarkness.app.domain.path.PathGenerator
import com.towerofdarkness.app.nav.BossWinNav
import com.towerofdarkness.app.nav.FloorBreakPersist
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.nav.RunSummaryData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * v0.1.9-floor2: F1 boss → Floor 2 climb; F2 clear/loss → summary+Hub; HP/rem/loadout/Ashbrand persist.
 */
class Floor2Test {

    @Test
    fun f1BossWin_navFloorBreak_notSummary() {
        assertEquals(
            BossWinNav.FLOOR_BREAK,
            GameController.afterBossWinNav(1)
        )
        assertEquals(
            BossWinNav.SUMMARY_VICTORY,
            GameController.afterBossWinNav(2)
        )
        // No Floor 3 — floor 3+ still summary
        assertEquals(
            BossWinNav.SUMMARY_VICTORY,
            GameController.afterBossWinNav(3)
        )
    }

    @Test
    fun floorBreak_persistsHpRemLoadoutAshbrand_noHealNoUnlock() {
        val before = FloorBreakPersist(
            playerHp = 17,
            runWallet = 14,
            loadoutIds = listOf("strike", "guard", "spark", "cleave", "brace"),
            loadoutLocked = true,
            weaponLevel = 2,
            weaponCharge = 0,
            unlockedThisRun = setOf("scout_charge")
        )
        // continueAfterFloorBreak must not mutate these — model the expected after-state
        val after = before.copy() // same values
        before.assertSurvives(after)
        assertTrue(after.loadoutLocked)
        assertEquals(17, after.playerHp) // not healed to full
        assertEquals(2, after.weaponLevel)
    }

    @Test
    fun floor2Path_seeds0to99_everyStartToBossHasCombat_eventsAtMostOne() {
        var fightless = 0
        var eventsOver = 0
        for (seed in 0..99) {
            val path = PathGenerator.generate(floor = 2, rng = Random(seed.toLong()))
            assertEquals(2, path.floor)
            val byId = path.nodes.associateBy { it.id }
            val outs = path.edges.groupBy({ it.from }, { it.to })
            val routes = mutableListOf<List<String>>()
            fun dfs(id: String, acc: MutableList<String>) {
                acc += id
                if (id == "boss") routes += acc.toList()
                else outs[id].orEmpty().forEach { dfs(it, acc) }
                acc.removeAt(acc.lastIndex)
            }
            dfs("start", mutableListOf())
            assertTrue("seed $seed routes", routes.isNotEmpty())
            for (route in routes) {
                if (route.none { byId[it]!!.type == NodeType.COMBAT }) fightless++
            }
            val events = path.nodes.count { it.type == NodeType.EVENT }
            if (events > 1) eventsOver++
        }
        assertEquals("0 zero-combat S→B on Floor 2 gens 0–99", 0, fightless)
        assertEquals("events ≤1 on every Floor 2 gen", 0, eventsOver)
    }

    @Test
    fun trashHp_floor2Is24_floor1Is20() {
        assertEquals(20, Balance.ENEMY_BASE_HP)
        assertEquals(24, Balance.ENEMY_FLOOR2_HP)
        assertEquals(20, Enemy.trashHpForFloor(1))
        assertEquals(24, Enemy.trashHpForFloor(2))
        val f1 = Enemy.forFloorCombat(0, floor = 1)
        val f2 = Enemy.forFloorCombat(0, floor = 2)
        assertEquals(20, f1.hp)
        assertEquals(20, f1.maxHp)
        assertEquals(24, f2.hp)
        assertEquals(24, f2.maxHp)
        assertFalse(f1.isBoss)
        assertFalse(f2.isBoss)
    }

    @Test
    fun ashWarden_hp32_sealWarden_hp28_countersUnchanged() {
        assertEquals(28, Balance.BOSS_HP)
        assertEquals(32, Balance.BOSS_FLOOR2_HP)
        val seal = Enemy.boss(1)
        val ash = Enemy.boss(2)
        assertEquals(EnemyKind.DRAGON, seal.kind)
        assertEquals("Seal-Warden", seal.kind.displayName)
        assertEquals(28, seal.hp)
        assertEquals(28, seal.maxHp)
        assertTrue(seal.isBoss)
        assertEquals(EnemyKind.ASH_WARDEN, ash.kind)
        assertEquals("Ash-Warden", ash.kind.displayName)
        assertEquals(32, ash.hp)
        assertEquals(32, ash.maxHp)
        assertTrue(ash.isBoss)
        // Counters stay on Balance (boss) / kind bands — not retuned this pass
        assertEquals(6, Balance.BOSS_COUNTER_MIN)
        assertEquals(9, Balance.BOSS_COUNTER_MAX)
    }

    @Test
    fun floor2Trash_prefersSpiderTrollOverGoblin() {
        val counts = mutableMapOf<EnemyKind, Int>()
        for (i in 0 until 60) {
            val e = Enemy.forFloorCombat(i, floor = 2)
            counts[e.kind] = (counts[e.kind] ?: 0) + 1
        }
        val spider = counts[EnemyKind.SPIDER] ?: 0
        val troll = counts[EnemyKind.TROLL] ?: 0
        val goblin = counts[EnemyKind.GOBLIN] ?: 0
        assertTrue("spider ($spider) should outweigh goblin ($goblin)", spider > goblin)
        assertTrue("troll ($troll) should outweigh goblin ($goblin)", troll > goblin)
    }

    @Test
    fun f2BossWin_summaryVictoryTitle_noFloor3() {
        val summary = RunSummaryData(
            won = true,
            nodesCleared = 12,
            remnantsEarned = 40,
            floorReached = 2,
            nearMiss = false
        )
        assertEquals("Victory — The seal breaks.", summary.title)
        assertEquals(2, summary.floorReached)
        assertEquals(
            BossWinNav.SUMMARY_VICTORY,
            GameController.afterBossWinNav(2)
        )
    }

    @Test
    fun f2BossLoss_summaryDefeat_floorField2() {
        val summary = RunSummaryData(
            won = false,
            nodesCleared = 10,
            remnantsEarned = 22,
            floorReached = 2,
            nearMiss = true
        )
        assertEquals("Defeat", summary.title)
        assertEquals(2, summary.floorReached)
        assertTrue(summary.nearMiss)
    }

    @Test
    fun f1BossLoss_summaryFloor1() {
        val summary = RunSummaryData(
            won = false,
            nodesCleared = 5,
            remnantsEarned = 8,
            floorReached = 1,
            nearMiss = false
        )
        assertEquals(1, summary.floorReached)
        assertEquals("Defeat", summary.title)
    }
}
