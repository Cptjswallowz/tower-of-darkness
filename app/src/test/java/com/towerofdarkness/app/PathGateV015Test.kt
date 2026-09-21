package com.towerofdarkness.app

import com.towerofdarkness.app.domain.path.NodeType
import com.towerofdarkness.app.domain.path.PathGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * QA GATE v0.1.5-path: 100-floor PathGenerator simulation (seeds 0..99).
 * Prints measured counts; asserts CoS gates.
 */
class PathGateV015Test {

    @Test
    fun pathGate_v015_100floorSim_seeds0to99() {
        val seedRange = 0..99
        val combatBeforeBossCounts = mutableListOf<Int>()
        val eventCounts = mutableListOf<Int>()
        val outliers = mutableListOf<String>()
        var floorsWithZeroCombat = 0
        val eventHistogram = mutableMapOf<Int, Int>()
        val combatHistogram = mutableMapOf<Int, Int>()

        println("=== PATH GATE v0.1.5-path 100-floor sim ===")
        println("seeds: ${seedRange.first}..${seedRange.last} (inclusive), floor=1")
        println("combat = all non-boss, non-START COMBAT nodes (mid-branch + merge if combat)")

        for (seed in seedRange) {
            val path = PathGenerator.generate(floor = 1, rng = Random(seed.toLong()))
            val beforeBoss = path.nodes.filter {
                it.type != NodeType.START && it.type != NodeType.BOSS
            }
            val combatCount = beforeBoss.count { it.type == NodeType.COMBAT }
            val eventCount = path.nodes.count { it.type == NodeType.EVENT }

            combatBeforeBossCounts += combatCount
            eventCounts += eventCount
            combatHistogram[combatCount] = (combatHistogram[combatCount] ?: 0) + 1
            eventHistogram[eventCount] = (eventHistogram[eventCount] ?: 0) + 1

            if (combatCount == 0) floorsWithZeroCombat++

            // Structural sanity / outliers
            val bossNodes = path.nodes.filter { it.type == NodeType.BOSS }
            val startNodes = path.nodes.filter { it.type == NodeType.START }
            val merge = path.nodes.firstOrNull { it.id == "merge" }
            val branchCount = path.nodes.count {
                it.id.startsWith("b") && it.type != NodeType.START &&
                    it.type != NodeType.BOSS && it.id != "merge"
            }
            val depthRows = path.nodes.filter {
                it.id.startsWith("b") && "_" in it.id
            }.map { it.row }.distinct().sorted()

            if (bossNodes.size != 1) {
                outliers += "seed $seed: boss_count=${bossNodes.size}"
            }
            if (startNodes.size != 1) {
                outliers += "seed $seed: start_count=${startNodes.size}"
            }
            if (merge == null) {
                outliers += "seed $seed: missing merge"
            }
            if (eventCount > 1) {
                outliers += "seed $seed: events=$eventCount >1 types=${beforeBoss.map { it.type }}"
            }
            if (combatCount == 0) {
                outliers += "seed $seed: ZERO combat before boss types=${beforeBoss.map { "${it.id}:${it.type}" }}"
            }
            if (branchCount !in 4..9) {
                // depth 2–3 × branches 2–3 → mid nodes 4–9
                outliers += "seed $seed: unusual mid-branch node count=$branchCount"
            }
            if (path.nodes.none { it.id == "boss" } ||
                path.edges.none { it.to == "boss" }
            ) {
                outliers += "seed $seed: boss not reachable via edge"
            }

            println(
                "seed=$seed combat_before_boss=$combatCount events=$eventCount " +
                    "nodes=${path.nodes.size} mid=$branchCount merge=${merge?.type} depthRows=$depthRows"
            )
        }

        println("--- SUMMARY ---")
        println("floors_with_zero_combat_before_boss=$floorsWithZeroCombat")
        println("combat_before_boss_histogram=$combatHistogram")
        println("event_count_histogram=$eventHistogram")
        println("event_max=${eventCounts.maxOrNull()} event_floors_gt1=${eventCounts.count { it > 1 }}")
        println("outliers_count=${outliers.size}")
        outliers.forEach { println("OUTLIER: $it") }

        // CoS gates
        assertEquals(
            "CoS: 0 floors with 0 combats before boss",
            0,
            floorsWithZeroCombat
        )
        assertTrue(
            "events must be ≤1 on every floor; histogram=$eventHistogram",
            eventCounts.all { it <= 1 }
        )
        assertTrue(
            "no structural outliers expected; got $outliers",
            outliers.none {
                it.contains("ZERO combat") || it.contains("events=") ||
                    it.contains("missing merge") || it.contains("boss_count") ||
                    it.contains("start_count") || it.contains("boss not reachable")
            }
        )
    }
}
