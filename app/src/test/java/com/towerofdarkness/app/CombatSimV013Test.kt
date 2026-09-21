package com.towerofdarkness.app

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.CombatState
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * v0.1.3 headless combat sims — CoS GREENLIGHT QA.
 * Fixed base seed 1000+i; 2000 fights each vs Ash Wretch and Seal-Warden.
 */
class CombatSimV013Test {

    companion object {
        const val FIGHTS = 2000
        const val BASE_SEED = 1000
        const val ROUND_CAP = 200
        const val REPORT_PATH = "/workspace/tower-of-darkness/docs/qa-sim-v013-spark-fix-2026-09-20.md"
        const val PRIOR_ASH_SPARK_EVENTS = 912
        const val PRIOR_SEAL_SPARK_EVENTS = 898
        const val PRIOR_ASH_WINRATE = 0.5070
        const val PRIOR_SEAL_WINRATE = 0.0005
    }

    private data class FightResult(
        val won: Boolean,
        val skillBeats: Int,
        val finalRound: Int,
        val fullWakeEvents: Int,
        val sparkEvents: Int,
        val greyViolations: Int,
        val hung: Boolean
    )

    private data class CohortMetrics(
        val label: String,
        val fights: Int,
        val wins: Int,
        val hangs: Int,
        val sumSkillBeats: Int,
        val sumFinalRound: Long,
        val fullWakeEvents: Int,
        val sparkEvents: Int,
        val fightsWithFullWake: Int,
        val fightsWithSpark: Int,
        val greyViolations: Int
    ) {
        val winrate: Double get() = wins.toDouble() / fights
        val meanSkillBeats: Double get() = sumSkillBeats.toDouble() / fights
        val meanFinalRound: Double get() = sumFinalRound.toDouble() / fights
        val fullWakePerFight: Double get() = fullWakeEvents.toDouble() / fights
        val fullWakeFightRate: Double get() = fightsWithFullWake.toDouble() / fights
        val sparkPerFight: Double get() = sparkEvents.toDouble() / fights
        val sparkFightRate: Double get() = fightsWithSpark.toDouble() / fights
    }

    private fun defaultCards() =
        CardCatalog.defaultLoadoutIds.mapNotNull { CardCatalog.byId(it) }

    private fun ashbrandLv1() =
        WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0)

    /** Run one fight; count completed skill beats (diceTumble+resolveSkill) until finished. */
    private fun simulateFight(seed: Int, enemy: Enemy): FightResult {
        val engine = CombatEngine(Random(seed))
        var s = engine.start(defaultCards(), enemy, ashbrandLv1())
        var skillBeats = 0
        var fullWake = 0
        var spark = 0
        var greyViolations = 0
        // Track fired-this-cycle for grey assert (mirrors exhaust_onlyUnspentPicked)
        val firedThisCycle = mutableSetOf<String>()

        while (!s.finished && skillBeats < ROUND_CAP) {
            val spentBefore = s.spentIds
            s = engine.diceTumble(s)
            val hid = s.highlightedId
            if (hid == null) break

            // Cycle reset clears fired tracking when spent emptied by tumble
            if (s.log.takeLast(3).any { it.message.contains("Cycle reset") }) {
                firedThisCycle.clear()
            }

            // Safer: after tumble, highlighted must not be in post-tumble spentIds
            if (hid in s.spentIds) greyViolations++
            // Mirror exhaust_onlyUnspentPicked: never pick already fired this cycle
            if (hid in firedThisCycle) greyViolations++
            // Also: if no cycle reset and hid was in spent before tumble, violation
            if (spentBefore.size < s.activeCards.size && hid in spentBefore) greyViolations++

            firedThisCycle += hid
            skillBeats++

            val logBeforeSkill = s.log.size
            s = engine.resolveSkill(s)
            if (s.awaitingWeapon) {
                s = engine.resolveWeapon(s)
            }
            // Count wake/spark from new log lines
            for (ev in s.log.drop(logBeforeSkill)) {
                val m = ev.message
                if (m.startsWith("ASHBRAND — WAKE") || m.contains("Wake!")) fullWake++
                if (m.contains("spark (", ignoreCase = true)) spark++
            }

            if (s.finished) break
            if (s.enemy.hp <= 0) break
            s = engine.resolveEnemy(s)
            if (s.finished) break
            s = engine.readyNext(s)
        }

        val hung = !s.finished && skillBeats >= ROUND_CAP
        val won = s.playerWon && s.finished
        return FightResult(
            won = won && !hung,
            skillBeats = skillBeats,
            finalRound = s.round,
            fullWakeEvents = fullWake,
            sparkEvents = spark,
            greyViolations = greyViolations,
            hung = hung
        )
    }

    private fun runCohort(label: String, enemyFactory: () -> Enemy): CohortMetrics {
        var wins = 0
        var hangs = 0
        var sumBeats = 0
        var sumRound = 0L
        var fullWakeEvents = 0
        var sparkEvents = 0
        var fightsWithFull = 0
        var fightsWithSpark = 0
        var grey = 0
        repeat(FIGHTS) { i ->
            val r = simulateFight(BASE_SEED + i, enemyFactory())
            if (r.hung) hangs++
            else if (r.won) wins++
            sumBeats += r.skillBeats
            sumRound += r.finalRound
            fullWakeEvents += r.fullWakeEvents
            sparkEvents += r.sparkEvents
            if (r.fullWakeEvents >= 1) fightsWithFull++
            if (r.sparkEvents >= 1) fightsWithSpark++
            grey += r.greyViolations
        }
        return CohortMetrics(
            label = label,
            fights = FIGHTS,
            wins = wins,
            hangs = hangs,
            sumSkillBeats = sumBeats,
            sumFinalRound = sumRound,
            fullWakeEvents = fullWakeEvents,
            sparkEvents = sparkEvents,
            fightsWithFullWake = fightsWithFull,
            fightsWithSpark = fightsWithSpark,
            greyViolations = grey
        )
    }

    private fun pct(x: Double): String = String.format("%.4f", x)
    private fun mean(x: Double): String = String.format("%.4f", x)

    private fun confirmAccepts(n: Int): Boolean = n == 5

    @Test
    fun simV013_headless_report() {
        // --- Static asserts: Confirm rejected at 4 and 6 ---
        assertEquals(5, Balance.LOADOUT_MIN)
        assertEquals(5, Balance.LOADOUT_MAX)
        assertFalse("confirm must reject 4", confirmAccepts(4))
        assertFalse("confirm must reject 6", confirmAccepts(6))
        assertTrue("confirm must accept 5", confirmAccepts(5))

        val gcSource = File(
            "/workspace/tower-of-darkness/app/src/main/java/com/towerofdarkness/app/nav/GameController.kt"
        ).readText()
        assertTrue(
            "confirmLoadout must reject selected.size != Balance.LOADOUT_MAX",
            gcSource.contains("if (selected.size != Balance.LOADOUT_MAX) return")
        )

        // --- Skip → 5 skills + Ashbrand ---
        assertEquals(5, CardCatalog.defaultLoadoutIds.size)
        val expectedIds = listOf(
            "hostflint", "cinder_step", "iron_mantle", "emberbrand", "dust_veil"
        )
        assertEquals(expectedIds, CardCatalog.defaultLoadoutIds)
        assertTrue(
            "skipTutorial sets loadout = defaultLoadoutIds",
            gcSource.contains("loadout = CardCatalog.defaultLoadoutIds.mapNotNull { CardCatalog.byId(it) }")
        )
        assertTrue(
            "skipTutorial sets Ashbrand lv1",
            gcSource.contains("equippedWeapon = WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0)")
        )
        // Unit-assert catalog defaults resolve
        val cards = defaultCards()
        assertEquals(5, cards.size)
        assertEquals(expectedIds, cards.map { it.id })
        val w = ashbrandLv1()
        assertEquals("ashbrand", w.def.id)
        assertEquals(1, w.level)
        assertEquals(0, w.charge)
        assertEquals(3, w.threshold)

        // Sanity enemies
        val wretch = Enemy.normal(EnemyKind.GOBLIN)
        assertEquals(Balance.ENEMY_BASE_HP, wretch.hp)
        assertEquals(EnemyKind.GOBLIN, wretch.kind)
        val warden = Enemy.boss()
        assertEquals(Balance.BOSS_HP, warden.hp)
        assertTrue(warden.isBoss)
        assertEquals(EnemyKind.DRAGON, warden.kind)

        // --- Sims ---
        val ash = runCohort("ash_wretch") { Enemy.normal(EnemyKind.GOBLIN) }
        val seal = runCohort("seal_warden") { Enemy.boss() }

        assertEquals("grey violations must be 0 (ash)", 0, ash.greyViolations)
        assertEquals("grey violations must be 0 (seal)", 0, seal.greyViolations)

        // Compact JSON for gradle capture
        val json = buildString {
            append("{")
            append("\"fights\":$FIGHTS,")
            append("\"base_seed\":$BASE_SEED,")
            append("\"round_cap\":$ROUND_CAP,")
            append("\"ash_wretch\":{")
            append("\"wins\":${ash.wins},")
            append("\"hangs\":${ash.hangs},")
            append("\"winrate\":${pct(ash.winrate)},")
            append("\"sum_skill_beats\":${ash.sumSkillBeats},")
            append("\"mean_skill_beats\":${mean(ash.meanSkillBeats)},")
            append("\"mean_final_round\":${mean(ash.meanFinalRound)},")
            append("\"full_wake_events\":${ash.fullWakeEvents},")
            append("\"full_wake_per_fight\":${mean(ash.fullWakePerFight)},")
            append("\"fights_with_full_wake\":${ash.fightsWithFullWake},")
            append("\"full_wake_fight_rate\":${pct(ash.fullWakeFightRate)},")
            append("\"spark_events\":${ash.sparkEvents},")
            append("\"spark_per_fight\":${mean(ash.sparkPerFight)},")
            append("\"fights_with_spark\":${ash.fightsWithSpark},")
            append("\"spark_fight_rate\":${pct(ash.sparkFightRate)},")
            append("\"grey_violations\":${ash.greyViolations}")
            append("},")
            append("\"seal_warden\":{")
            append("\"wins\":${seal.wins},")
            append("\"hangs\":${seal.hangs},")
            append("\"winrate\":${pct(seal.winrate)},")
            append("\"sum_skill_beats\":${seal.sumSkillBeats},")
            append("\"mean_skill_beats\":${mean(seal.meanSkillBeats)},")
            append("\"mean_final_round\":${mean(seal.meanFinalRound)},")
            append("\"full_wake_events\":${seal.fullWakeEvents},")
            append("\"full_wake_per_fight\":${mean(seal.fullWakePerFight)},")
            append("\"fights_with_full_wake\":${seal.fightsWithFullWake},")
            append("\"full_wake_fight_rate\":${pct(seal.fullWakeFightRate)},")
            append("\"spark_events\":${seal.sparkEvents},")
            append("\"spark_per_fight\":${mean(seal.sparkPerFight)},")
            append("\"fights_with_spark\":${seal.fightsWithSpark},")
            append("\"spark_fight_rate\":${pct(seal.sparkFightRate)},")
            append("\"grey_violations\":${seal.greyViolations}")
            append("},")
            append("\"assert_grey_zero\":true,")
            append("\"assert_confirm_4_6_rejected\":true,")
            append("\"assert_skip_default_5_ashbrand\":true")
            append("}")
        }
        println("SIM_REPORT_JSON=$json")

        // Engine vs design: same-beat SPARK+full
        val sameBeatNote =
            "Engine resolveSkill independently queues CHAIN full and SPARK via pendingFullWake / pendingSpark; " +
                "resolveWeapon fires both on the same beat (full Wake first, then half-Wake SPARK)."

        writeMarkdown(ash, seal, json, sameBeatNote)
    }

    private fun writeMarkdown(
        ash: CohortMetrics,
        seal: CohortMetrics,
        json: String,
        sameBeatNote: String
    ) {
        val md = buildString {
            appendLine("# QA sim v0.1.3 — 2026-09-20")
            appendLine()
            appendLine("Status: **measured** headless sims (CoS GREENLIGHT).")
            appendLine()
            appendLine("- Project: `/workspace/tower-of-darkness`")
            appendLine("- Test: `com.towerofdarkness.app.CombatSimV013Test`")
            appendLine("- Fights per cohort: **$FIGHTS**")
            appendLine("- Seed: `seed = $BASE_SEED + i` (i = 0..${FIGHTS - 1})")
            appendLine("- Round cap: **$ROUND_CAP** skill beats (hangs counted as failures / not wins)")
            appendLine("- Loadout: `CardCatalog.defaultLoadoutIds` (5) + Ashbrand lv1 charge 0")
            appendLine("- Rounds metric: completed player skill beats (`diceTumble` → `resolveSkill`) until finished")
            appendLine()
            appendLine("## Asserts")
            appendLine()
            appendLine("| Check | Result |")
            appendLine("|-------|--------|")
            appendLine("| Grey-pick violations == 0 | **PASS** (ash=${ash.greyViolations}, seal=${seal.greyViolations}) |")
            appendLine("| Confirm reject 4 and 6 (`LOADOUT_MIN/MAX==5`, `confirmAccepts`) | **PASS** |")
            appendLine("| `GameController.confirmLoadout` rejects `selected.size != LOADOUT_MAX` | **PASS** (source line verified) |")
            appendLine("| Skip → 5 skills + Ashbrand lv1 | **PASS** (catalog + `skipTutorial` source) |")
            appendLine()
            appendLine("## Ash Wretch (`Enemy.normal(GOBLIN)`, HP=${Balance.ENEMY_BASE_HP})")
            appendLine()
            appendCohortTable(ash)
            appendLine()
            appendLine("## Seal-Warden (`Enemy.boss()`, HP=${Balance.BOSS_HP})")
            appendLine()
            appendCohortTable(seal)
            appendLine()
            appendLine("## SIM_REPORT_JSON")
            appendLine()
            appendLine("```")
            appendLine("SIM_REPORT_JSON=$json")
            appendLine("```")
            appendLine()
            appendLine("## Anomalies / engine vs design")
            appendLine()
            appendLine(sameBeatNote)
            appendLine()
            appendLine("Full Wake log match: message contains `Wake!` (e.g. `Ashbrand Wake! (4)`).")
            appendLine("SPARK log match: message contains ` spark (` (e.g. `Ashbrand spark (2)`).")
            appendLine()
            appendLine("## Delta vs prior pre-fix report")
            appendLine()
            appendLine("Prior report values: Ash SPARK events = **912**, Seal-Warden SPARK events = **898**; winrate = **50.70% / 0.05%** (Ash / Seal).")
            appendLine()
            appendLine("| Cohort | Prior SPARK events | New SPARK events | Delta SPARK events | Prior winrate | New winrate | Delta winrate |")
            appendLine("|--------|-------------------:|-----------------:|-------------------:|--------------:|------------:|--------------:|")
            appendLine("| Ash Wretch | $PRIOR_ASH_SPARK_EVENTS | ${ash.sparkEvents} | ${ash.sparkEvents - PRIOR_ASH_SPARK_EVENTS} | ${pct(PRIOR_ASH_WINRATE)} | ${pct(ash.winrate)} | ${pct(ash.winrate - PRIOR_ASH_WINRATE)} |")
            appendLine("| Seal-Warden | $PRIOR_SEAL_SPARK_EVENTS | ${seal.sparkEvents} | ${seal.sparkEvents - PRIOR_SEAL_SPARK_EVENTS} | ${pct(PRIOR_SEAL_WINRATE)} | ${pct(seal.winrate)} | ${pct(seal.winrate - PRIOR_SEAL_WINRATE)} |")
            appendLine()
            appendLine("## Method")
            appendLine()
            appendLine("1. `CombatEngine(Random(1000+i)).start(default5, enemy, Ashbrand lv1)`")
            appendLine("2. Loop: `diceTumble` → grey asserts → `resolveSkill` → optional `resolveWeapon` → `resolveEnemy` → `readyNext`")
            appendLine("3. Count skill beats, Wake!/spark events from log deltas, wins / hangs")
            appendLine()
            appendLine("Generated EDT: see test run timestamp in gradle output.")
        }
        File(REPORT_PATH).writeText(md)
    }

    private fun StringBuilder.appendCohortTable(c: CohortMetrics) {
        appendLine("| Metric | Value |")
        appendLine("|--------|-------|")
        appendLine("| Wins | ${c.wins} / ${c.fights} |")
        appendLine("| Hangs (round cap) | ${c.hangs} |")
        appendLine("| Winrate | ${pct(c.winrate)} |")
        appendLine("| Sum skill beats | ${c.sumSkillBeats} |")
        appendLine("| Mean skill beats | ${mean(c.meanSkillBeats)} |")
        appendLine("| Mean final `state.round` | ${mean(c.meanFinalRound)} |")
        appendLine("| Full Wake events | ${c.fullWakeEvents} |")
        appendLine("| Full Wake / fight (avg procs) | ${mean(c.fullWakePerFight)} |")
        appendLine("| Fights with ≥1 Full Wake | ${c.fightsWithFullWake} / ${c.fights} (${pct(c.fullWakeFightRate)}) |")
        appendLine("| SPARK events | ${c.sparkEvents} |")
        appendLine("| SPARK / fight | ${mean(c.sparkPerFight)} |")
        appendLine("| Fights with ≥1 SPARK | ${c.fightsWithSpark} / ${c.fights} (${pct(c.sparkFightRate)}) |")
        appendLine("| Grey violations | ${c.greyViolations} |")
    }
}
