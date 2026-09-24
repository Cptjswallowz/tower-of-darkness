package com.towerofdarkness.app

import com.towerofdarkness.app.data.MidRunSlot
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.EnemyLook
import com.towerofdarkness.app.domain.combat.HallwayPacks
import com.towerofdarkness.app.domain.combat.HallwayRole
import com.towerofdarkness.app.domain.combat.PackAuthorship
import com.towerofdarkness.app.domain.path.NodeType
import com.towerofdarkness.app.domain.path.PathGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

/**
 * v0.1.26-packs — titles, look pools, hallway weights, save/resume look persist.
 */
class PacksV0126Test {

    @Test
    fun titles_weakGoblinAndSturdyOrc() {
        assertEquals("Weak Goblin", EnemyKind.GOBLIN.displayName)
        assertEquals("Sturdy Orc", EnemyKind.ORC.displayName)
        // Seal Spinner / Stone Hunger band → Sturdy Orc title path
        assertEquals("Sturdy Orc", EnemyKind.SPIDER.displayName)
        assertEquals("Sturdy Orc", EnemyKind.TROLL.displayName)
        assertEquals("Seal-Warden", EnemyKind.DRAGON.displayName)
        assertEquals("Ash-Warden", EnemyKind.ASH_WARDEN.displayName)
    }

    @Test
    fun combatLogStrings_useDisplayTitles() {
        val goblin = Enemy.normal(EnemyKind.GOBLIN, look = EnemyLook.KNIFE)
        val orc = Enemy.normal(EnemyKind.ORC, look = EnemyLook.HAMMER)
        assertTrue("${goblin.kind.displayName} winds up…".startsWith("Weak Goblin"))
        assertTrue("${orc.kind.displayName} hits for 3".startsWith("Sturdy Orc"))
        assertFalse(goblin.kind.displayName.contains("Ash Wretch"))
        assertFalse(orc.kind.displayName.contains("Ruin Brute"))
    }

    @Test
    fun lookPools_equalThird_seeded() {
        val goblinCounts = mutableMapOf<EnemyLook, Int>()
        val orcCounts = mutableMapOf<EnemyLook, Int>()
        val n = 3000
        for (i in 0 until n) {
            val g = HallwayPacks.rollLook(HallwayRole.WEAK_GOBLIN, Random(i.toLong()))
            val o = HallwayPacks.rollLook(HallwayRole.STURDY_ORC, Random(i.toLong() + 99))
            goblinCounts[g] = (goblinCounts[g] ?: 0) + 1
            orcCounts[o] = (orcCounts[o] ?: 0) + 1
        }
        assertEquals(3, goblinCounts.size)
        assertEquals(3, orcCounts.size)
        EnemyLook.goblinLooks().forEach { look ->
            val c = goblinCounts[look]!!
            assertTrue("$look count $c near $n/3", abs(c - n / 3) < n / 10)
        }
        EnemyLook.orcLooks().forEach { look ->
            val c = orcCounts[look]!!
            assertTrue("$look count $c near $n/3", abs(c - n / 3) < n / 10)
        }
    }

    @Test
    fun floor1Weights_goblin75_orc25() {
        assertEquals(75, HallwayPacks.goblinWeightPct(1))
        assertEquals(25, HallwayPacks.orcWeightPct(1))
        val n = 4000
        var goblin = 0
        var orc = 0
        for (i in 0 until n) {
            when (HallwayPacks.rollRole(1, Random(i.toLong() * 17))) {
                HallwayRole.WEAK_GOBLIN -> goblin++
                HallwayRole.STURDY_ORC -> orc++
            }
        }
        val gPct = goblin * 100.0 / n
        assertTrue("F1 goblin pct $gPct ~75", abs(gPct - 75.0) < 4.0)
        assertTrue("F1 orc pct ${orc * 100.0 / n} ~25", abs(orc * 100.0 / n - 25.0) < 4.0)
    }

    @Test
    fun floor2Weights_goblin30_orc70() {
        assertEquals(30, HallwayPacks.goblinWeightPct(2))
        assertEquals(70, HallwayPacks.orcWeightPct(2))
        val n = 4000
        var goblin = 0
        var orc = 0
        for (i in 0 until n) {
            when (HallwayPacks.rollRole(2, Random(i.toLong() * 31))) {
                HallwayRole.WEAK_GOBLIN -> goblin++
                HallwayRole.STURDY_ORC -> orc++
            }
        }
        assertTrue("F2 goblin pct ${goblin * 100.0 / n} ~30", abs(goblin * 100.0 / n - 30.0) < 4.0)
        assertTrue("F2 orc pct ${orc * 100.0 / n} ~70", abs(orc * 100.0 / n - 70.0) < 4.0)
    }

    @Test
    fun hallwayTable_onlyGoblinOrOrc_neverBoss() {
        for (floor in 1..2) {
            for (i in 0 until 200) {
                val e = Enemy.forFloorCombat(i, floor)
                assertTrue(e.kind == EnemyKind.GOBLIN || e.kind == EnemyKind.ORC)
                assertFalse(e.isBoss)
                assertNotNull(e.look)
                assertNull(Enemy.boss(floor).look)
                assertTrue(HallwayPacks.isBossKind(Enemy.boss(floor).kind))
                assertFalse(HallwayPacks.isHallwayKind(Enemy.boss(floor).kind))
            }
        }
    }

    @Test
    fun bossesNeverDrawnFromHallwayTable() {
        assertEquals(EnemyKind.DRAGON, Enemy.boss(1).kind)
        assertEquals(EnemyKind.ASH_WARDEN, Enemy.boss(2).kind)
        assertNull(Enemy.boss(1).look)
        assertNull(Enemy.boss(2).look)
        // Hallway roll never yields DRAGON / ASH_WARDEN
        for (i in 0 until 500) {
            val e = HallwayPacks.roll(1, Random(i.toLong()))
            assertTrue(e.kind == EnemyKind.GOBLIN || e.kind == EnemyKind.ORC)
        }
    }

    @Test
    fun pathCombatNodes_authorLook_persistAcrossMidRunSave() {
        val path = PathGenerator.generate(floor = 1, rng = Random(42_126L))
        val combatNodes = path.nodes.filter { it.type == NodeType.COMBAT }
        assertTrue(combatNodes.isNotEmpty())
        combatNodes.forEach { n ->
            assertNotNull("packKind on ${n.id}", n.packKind)
            assertNotNull("packLook on ${n.id}", n.packLook)
            assertTrue(n.packKind == "GOBLIN" || n.packKind == "ORC")
            assertNotNull(EnemyLook.fromId(n.packLook))
        }
        // Boss / non-combat have no pack
        path.nodes.filter { it.type != NodeType.COMBAT }.forEach { n ->
            assertNull(n.packKind)
            assertNull(n.packLook)
        }

        val snap = MidRunSlot.fromPath(path, "packs_test")
        val encoded = MidRunSlot.encode(
            MidRunSlot(
                runId = "run-packs",
                rngSeed = 42_126L,
                floor = 1,
                pendingStairContinue = false,
                path = snap,
                playerHp = 20,
                playerMaxHp = 30,
                runWallet = 0,
                runRemnantsEarned = 0,
                nodesCleared = 0,
                loadout = com.towerofdarkness.app.data.MidRunLoadout(false, listOf("strike")),
                ashbrand = com.towerofdarkness.app.data.MidRunAshbrand("ashbrand", 1, 0),
                freeScoutCharges = 0,
                rumorRerolls = 0,
                combatSpeed2x = false,
                unlocksThisRun = emptyList(),
                shopVisits = emptyList(),
                resume = com.towerofdarkness.app.data.MidRunResume.Path
            )
        )
        val decoded = MidRunSlot.decode(encoded)!!
        val restored = MidRunSlot.toTowerPath(decoded.path)
        combatNodes.forEach { original ->
            val again = restored.node(original.id)
            assertEquals(original.packKind, again.packKind)
            assertEquals(original.packLook, again.packLook)
        }
    }

    @Test
    fun sameFightLook_persistsOnEnemyAndPackAuthorshipRoundTrip() {
        val pack = PackAuthorship(HallwayRole.WEAK_GOBLIN, EnemyLook.BOTTLE)
        val enemy = pack.toEnemy(1)
        assertEquals(EnemyLook.BOTTLE, enemy.look)
        assertEquals("Weak Goblin", enemy.kind.displayName)
        val raw = PackAuthorship.encode(pack)
        val again = PackAuthorship.decode(raw)!!
        assertEquals(pack, again)
        assertEquals(EnemyLook.BOTTLE, again.toEnemy(1).look)
    }

    @Test
    fun nextHallwayCombat_rerollsLook() {
        val path = PathGenerator.generate(floor = 1, rng = Random(7_777L))
        val looks = path.nodes.filter { it.type == NodeType.COMBAT }.map { it.packLook }
        // With ≥2 combat nodes, looks need not all match (re-roll per node).
        // Even with 1 node, a second floor gen / second roll differs for different seeds.
        val a = HallwayPacks.roll(1, Random(1L))
        val b = HallwayPacks.roll(1, Random(2L))
        // Different seeds → independent authorship (may coincidentally match; soft check via many)
        var differs = false
        for (i in 0 until 50) {
            val x = HallwayPacks.roll(1, Random(i.toLong()))
            val y = HallwayPacks.roll(1, Random(i.toLong() + 10_000))
            if (x.look != y.look || x.role != y.role) {
                differs = true
                break
            }
        }
        assertTrue("next combat re-rolls", differs)
        assertTrue(looks.isNotEmpty())
        // Suppress unused when single combat path
        assertNotEquals("", a.look.id)
        assertNotEquals("", b.look.id)
    }

    @Test
    fun drawableHooks_ready_noInventedArt() {
        val names = HallwayPacks.allDrawableNames()
        assertEquals(6, names.size)
        assertTrue(names.contains("pack_weak_goblin_knife"))
        assertTrue(names.contains("pack_sturdy_orc_hammer"))
        // No PNGs dropped yet → not shipped
        names.forEach { assertFalse("unexpected art $it", com.towerofdarkness.app.domain.combat.BodyArt.packArtShipped(it)) }
        assertTrue(com.towerofdarkness.app.domain.combat.BodyArt.hallwayEnemyUsesPlaceholder())
    }

    @Test
    fun hpAndCountersFrozen() {
        assertEquals(20, Enemy.trashHpForFloor(1))
        assertEquals(24, Enemy.trashHpForFloor(2))
        assertEquals(7, EnemyKind.GOBLIN.trashCounterMin)
        assertEquals(9, EnemyKind.GOBLIN.trashCounterMax)
        assertEquals(7, EnemyKind.ORC.trashCounterMin)
        assertEquals(9, EnemyKind.ORC.trashCounterMax)
        assertEquals(28, Enemy.boss(1).hp)
        assertEquals(32, Enemy.boss(2).hp)
    }
}
