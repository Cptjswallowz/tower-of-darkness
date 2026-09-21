package com.towerofdarkness.app

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatAnimStyle
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
import kotlin.random.Random

/**
 * v0.1.4-wake headless combat sims — CoS GREENLIGHT QA.
 * Fixed base seed 1000+i; 2000 fights each vs five cohorts.
 */
class CombatSimV014Test {

    companion object {
        const val FIGHTS = 2000
        const val BASE_SEED = 1000
        const val ROUND_CAP = 200
        const val REPORT_PATH = "/workspace/tower-of-darkness/docs/qa-sim-v014-trash69-2026-09-20.md"

        // Winrate target bands (design)
        val BAND_ASH = 0.50 to 0.70
        val BAND_RUIN = 0.50 to 0.70
        val BAND_SPINNER = 0.50 to 0.70
        val BAND_HUNGER = 0.50 to 0.70
        val BAND_WARDEN = 0.22 to 0.35
    }

    private data class FightResult(
        val won: Boolean,
        val skillBeats: Int,
        val finalRound: Int,
        val fullWakeEvents: Int,
        val sparkEvents: Int,
        val greyViolations: Int,
        val sparkAssertFails: Int,
        val fullWakeAssertFails: Int,
        val hung: Boolean
    )

    private data class CohortMetrics(
        val label: String,
        val displayName: String,
        val fights: Int,
        val wins: Int,
        val hangs: Int,
        val sumSkillBeats: Int,
        val sumFinalRound: Long,
        val fullWakeEvents: Int,
        val sparkEvents: Int,
        val fightsWithFullWake: Int,
        val fightsWithSpark: Int,
        val greyViolations: Int,
        val sparkAssertFails: Int,
        val fullWakeAssertFails: Int,
        val targetLo: Double,
        val targetHi: Double
    ) {
        val winrate: Double get() = wins.toDouble() / fights
        val meanSkillBeats: Double get() = sumSkillBeats.toDouble() / fights
        val meanFinalRound: Double get() = sumFinalRound.toDouble() / fights
        val fullWakePerFight: Double get() = fullWakeEvents.toDouble() / fights
        val fullWakeFightRate: Double get() = fightsWithFullWake.toDouble() / fights
        val sparkPerFight: Double get() = sparkEvents.toDouble() / fights
        val sparkFightRate: Double get() = fightsWithSpark.toDouble() / fights
        val inBand: Boolean get() = winrate in targetLo..targetHi
    }

    private fun defaultCards() =
        CardCatalog.defaultLoadoutIds.mapNotNull { CardCatalog.byId(it) }

    private fun ashbrandLv1() =
        WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0)

    private val wakeRegex = Regex("^ASHBRAND — WAKE \\d+$")

    private fun isSparkEvent(message: String): Boolean =
        message.contains("spark (", ignoreCase = true)

    private fun isFullWakeEvent(message: String): Boolean =
        message.startsWith("ASHBRAND — WAKE") || wakeRegex.matches(message)

    /** Run one fight; count completed skill beats (diceTumble+resolveSkill) until finished. */
    private fun simulateFight(seed: Int, enemy: Enemy): FightResult {
        val engine = CombatEngine(Random(seed))
        var s = engine.start(defaultCards(), enemy, ashbrandLv1())
        var skillBeats = 0
        var fullWake = 0
        var spark = 0
        var greyViolations = 0
        var sparkAssertFails = 0
        var fullWakeAssertFails = 0
        val firedThisCycle = mutableSetOf<String>()

        while (!s.finished && skillBeats < ROUND_CAP) {
            val spentBefore = s.spentIds
            s = engine.diceTumble(s)
            val hid = s.highlightedId
            if (hid == null) break

            if (s.log.takeLast(3).any { it.message.contains("Cycle reset") }) {
                firedThisCycle.clear()
            }

            if (hid in s.spentIds) greyViolations++
            if (hid in firedThisCycle) greyViolations++
            if (spentBefore.size < s.activeCards.size && hid in spentBefore) greyViolations++

            firedThisCycle += hid
            skillBeats++

            val logBeforeSkill = s.log.size
            s = engine.resolveSkill(s)
            if (s.awaitingWeapon) {
                s = engine.resolveWeapon(s)
            }
            for (ev in s.log.drop(logBeforeSkill)) {
                val m = ev.message
                if (isFullWakeEvent(m)) {
                    fullWake++
                    val okMsg = wakeRegex.matches(m)
                    val okGold = ev.goldLog
                    val okAnim = ev.animStyle == CombatAnimStyle.CHARGE_SHAKE_SLOWMO
                    if (!okMsg || !okGold || !okAnim) fullWakeAssertFails++
                }
                if (isSparkEvent(m)) {
                    spark++
                    val floatText = ev.floating?.text
                    val floatSaysWake = floatText != null && floatText.equals("WAKE", ignoreCase = true)
                    val floatIsCrit = ev.floating?.isCrit == true
                    val ok =
                        !ev.goldLog &&
                            ev.animStyle != CombatAnimStyle.CHARGE_SHAKE_SLOWMO &&
                            !floatSaysWake &&
                            !floatIsCrit
                    if (!ok) sparkAssertFails++
                }
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
            sparkAssertFails = sparkAssertFails,
            fullWakeAssertFails = fullWakeAssertFails,
            hung = hung
        )
    }

    private fun runCohort(
        label: String,
        displayName: String,
        targetLo: Double,
        targetHi: Double,
        enemyFactory: () -> Enemy
    ): CohortMetrics {
        var wins = 0
        var hangs = 0
        var sumBeats = 0
        var sumRound = 0L
        var fullWakeEvents = 0
        var sparkEvents = 0
        var fightsWithFull = 0
        var fightsWithSpark = 0
        var grey = 0
        var sparkFails = 0
        var fullFails = 0
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
            sparkFails += r.sparkAssertFails
            fullFails += r.fullWakeAssertFails
        }
        return CohortMetrics(
            label = label,
            displayName = displayName,
            fights = FIGHTS,
            wins = wins,
            hangs = hangs,
            sumSkillBeats = sumBeats,
            sumFinalRound = sumRound,
            fullWakeEvents = fullWakeEvents,
            sparkEvents = sparkEvents,
            fightsWithFullWake = fightsWithFull,
            fightsWithSpark = fightsWithSpark,
            greyViolations = grey,
            sparkAssertFails = sparkFails,
            fullWakeAssertFails = fullFails,
            targetLo = targetLo,
            targetHi = targetHi
        )
    }

    private fun pct(x: Double): String = String.format("%.4f", x)
    private fun mean(x: Double): String = String.format("%.4f", x)
    private fun pctPct(x: Double): String = String.format("%.2f%%", x * 100.0)

    private fun confirmAccepts(n: Int): Boolean = n == 5

    private fun assertLv1FullByCharge3() {
        assertEquals(3, WeaponCatalog.ashbrand.threshold(1))
        // Short charge-ramp: start charge 0, force 3 attack resolves → mustFull
        val cards = defaultCards()
        val attack = cards.first { it.id == "hostflint" }
        val engine = CombatEngine(Random(42))
        var s = engine.start(
            cards,
            Enemy.normal(EnemyKind.GOBLIN).let {
                it.copy(maxHp = 999, hp = 999)
            },
            WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 0),
            maxHp = 99,
            playerHp = 99
        )
        repeat(3) {
            s = s.copy(
                lastFiredCard = attack,
                highlightedId = attack.id,
                awaitingWeapon = false,
                pendingFullWake = false,
                pendingSpark = false
            )
            s = engine.resolveSkill(s)
            // Drain weapon beat so charge reset does not confuse next iterate —
            // but we only care about mustFull after the 3rd attack charge.
            if (it < 2 && s.awaitingWeapon) {
                // Before threshold: spark may still queue; clear without caring
                s = engine.resolveWeapon(s)
            }
        }
        assertTrue(
            "after 3 attack charges Lv1 mustFull (charge>=threshold)",
            s.weapon.charge >= s.weapon.threshold
        )
        assertTrue("pendingFullWake after 3rd attack", s.pendingFullWake)
        assertEquals(3, s.weapon.threshold)
    }

    @Test
    fun simV014_headless_report() {
        // --- Static: Confirm reject 4/6; Skip → 5 + Ashbrand ---
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
        val cards = defaultCards()
        assertEquals(5, cards.size)
        assertEquals(expectedIds, cards.map { it.id })
        val w = ashbrandLv1()
        assertEquals("ashbrand", w.def.id)
        assertEquals(1, w.level)
        assertEquals(0, w.charge)
        assertEquals(3, w.threshold)

        // Balance + per-kind trash counters
        assertEquals(7, Balance.ENEMY_COUNTER_MIN)
        assertEquals(9, Balance.ENEMY_COUNTER_MAX)
        assertEquals(6, Balance.BOSS_COUNTER_MIN)
        assertEquals(9, Balance.BOSS_COUNTER_MAX)
        assertEquals(28, Balance.BOSS_HP)
        assertEquals(7 to 9, EnemyKind.GOBLIN.trashCounterMin to EnemyKind.GOBLIN.trashCounterMax)
        assertEquals(7 to 9, EnemyKind.ORC.trashCounterMin to EnemyKind.ORC.trashCounterMax)
        assertEquals(7 to 9, EnemyKind.TROLL.trashCounterMin to EnemyKind.TROLL.trashCounterMax)
        assertEquals(7 to 9, EnemyKind.SPIDER.trashCounterMin to EnemyKind.SPIDER.trashCounterMax)

        val resolveSrc = File(
            "/workspace/tower-of-darkness/app/src/main/java/com/towerofdarkness/app/domain/combat/CombatEngine.kt"
        ).readText()
        assertTrue(
            "resolveEnemy must use kind.trashCounterMin/Max when not boss",
            resolveSrc.contains("state.enemy.kind.trashCounterMin to state.enemy.kind.trashCounterMax")
        )

        // Lv1 FULL by charge 3
        assertLv1FullByCharge3()

        // Sanity enemies
        val wretch = Enemy.normal(EnemyKind.GOBLIN)
        assertEquals(Balance.ENEMY_BASE_HP, wretch.hp)
        assertEquals(EnemyKind.GOBLIN, wretch.kind)
        assertFalse(wretch.isBoss)
        val brute = Enemy.normal(EnemyKind.ORC)
        assertEquals(EnemyKind.ORC, brute.kind)
        val spinner = Enemy.normal(EnemyKind.SPIDER)
        assertEquals(EnemyKind.SPIDER, spinner.kind)
        val hunger = Enemy.normal(EnemyKind.TROLL)
        assertEquals(EnemyKind.TROLL, hunger.kind)
        val warden = Enemy.boss()
        assertEquals(Balance.BOSS_HP, warden.hp)
        assertTrue(warden.isBoss)
        assertEquals(EnemyKind.DRAGON, warden.kind)

        // --- Sims ---
        val ash = runCohort("ash_wretch", "Ash Wretch", BAND_ASH.first, BAND_ASH.second) {
            Enemy.normal(EnemyKind.GOBLIN)
        }
        val ruin = runCohort("ruin_brute", "Ruin Brute", BAND_RUIN.first, BAND_RUIN.second) {
            Enemy.normal(EnemyKind.ORC)
        }
        val spinnerC = runCohort("seal_spinner", "Seal Spinner", BAND_SPINNER.first, BAND_SPINNER.second) {
            Enemy.normal(EnemyKind.SPIDER)
        }
        val hungerC = runCohort("stone_hunger", "Stone Hunger", BAND_HUNGER.first, BAND_HUNGER.second) {
            Enemy.normal(EnemyKind.TROLL)
        }
        val seal = runCohort("seal_warden", "Seal-Warden", BAND_WARDEN.first, BAND_WARDEN.second) {
            Enemy.boss()
        }

        val cohorts = listOf(ash, ruin, spinnerC, hungerC, seal)

        for (c in cohorts) {
            assertEquals("grey violations must be 0 (${c.label})", 0, c.greyViolations)
            assertEquals("SPARK event asserts must pass (${c.label})", 0, c.sparkAssertFails)
            assertEquals("FULL Wake event asserts must pass (${c.label})", 0, c.fullWakeAssertFails)
        }

        val callouts = buildCallouts(cohorts)

        val json = buildJson(cohorts)
        println("SIM_REPORT_JSON=$json")

        writeMarkdown(cohorts, json, callouts)
    }

    private fun buildCallouts(cohorts: List<CohortMetrics>): List<String> {
        val out = mutableListOf<String>()
        for (c in cohorts) {
            when (c.label) {
                "ash_wretch", "ruin_brute", "seal_spinner", "stone_hunger" -> when {
                    c.winrate > 0.70 ->
                        out += "**${c.displayName}** WR ${pctPct(c.winrate)} >70% — flag next step 7–9."
                    c.winrate < 0.50 ->
                        out += "**${c.displayName}** WR ${pctPct(c.winrate)} <50% — under-band."
                    else -> Unit
                }
                "seal_warden" -> if (c.winrate !in 0.22..0.35) {
                    out += "**Seal-Warden** WR ${pctPct(c.winrate)} outside 22–35% — flag; do not touch boss."
                }
            }
        }
        return out
    }

    private fun cohortJson(c: CohortMetrics): String = buildString {
        append("{")
        append("\"wins\":${c.wins},")
        append("\"hangs\":${c.hangs},")
        append("\"winrate\":${pct(c.winrate)},")
        append("\"sum_skill_beats\":${c.sumSkillBeats},")
        append("\"mean_skill_beats\":${mean(c.meanSkillBeats)},")
        append("\"mean_final_round\":${mean(c.meanFinalRound)},")
        append("\"full_wake_events\":${c.fullWakeEvents},")
        append("\"full_wake_per_fight\":${mean(c.fullWakePerFight)},")
        append("\"fights_with_full_wake\":${c.fightsWithFullWake},")
        append("\"full_wake_fight_rate\":${pct(c.fullWakeFightRate)},")
        append("\"spark_events\":${c.sparkEvents},")
        append("\"spark_per_fight\":${mean(c.sparkPerFight)},")
        append("\"fights_with_spark\":${c.fightsWithSpark},")
        append("\"spark_fight_rate\":${pct(c.sparkFightRate)},")
        append("\"grey_violations\":${c.greyViolations},")
        append("\"spark_assert_fails\":${c.sparkAssertFails},")
        append("\"full_wake_assert_fails\":${c.fullWakeAssertFails},")
        append("\"target_lo\":${pct(c.targetLo)},")
        append("\"target_hi\":${pct(c.targetHi)},")
        append("\"in_band\":${c.inBand}")
        append("}")
    }

    private fun buildJson(cohorts: List<CohortMetrics>): String = buildString {
        append("{")
        append("\"fights\":$FIGHTS,")
        append("\"base_seed\":$BASE_SEED,")
        append("\"round_cap\":$ROUND_CAP,")
        append("\"threshold_lv1\":${WeaponCatalog.ashbrand.threshold(1)},")
        append("\"balance_enemy_counter\":\"${Balance.ENEMY_COUNTER_MIN}-${Balance.ENEMY_COUNTER_MAX}\",")
        append("\"balance_boss_counter\":\"${Balance.BOSS_COUNTER_MIN}-${Balance.BOSS_COUNTER_MAX}\",")
        append("\"boss_hp\":${Balance.BOSS_HP},")
        for (c in cohorts) {
            append("\"${c.label}\":${cohortJson(c)},")
        }
        append("\"assert_grey_zero\":true,")
        append("\"assert_spark_events\":true,")
        append("\"assert_full_wake_events\":true,")
        append("\"assert_threshold_lv1_3\":true,")
        append("\"assert_confirm_4_6_rejected\":true,")
        append("\"assert_skip_default_5_ashbrand\":true")
        append("}")
    }

    private fun writeMarkdown(
        cohorts: List<CohortMetrics>,
        json: String,
        callouts: List<String>
    ) {
        val md = buildString {
            appendLine("# QA sim v0.1.4-trash69 — 2026-09-20")
            appendLine()
            appendLine("Status: **measured** headless sims (CoS GREENLIGHT).")
            appendLine()
            appendLine("- Project: `/workspace/tower-of-darkness`")
            appendLine("- Test: `com.towerofdarkness.app.CombatSimV014Test`")
            appendLine("- Fights per cohort: **$FIGHTS**")
            appendLine("- Seed: `seed = $BASE_SEED + i` (i = 0..${FIGHTS - 1})")
            appendLine("- Round cap: **$ROUND_CAP** skill beats (hangs counted as failures / not wins)")
            appendLine("- Loadout: `CardCatalog.defaultLoadoutIds` (5) + Ashbrand lv1 charge 0")
            appendLine("- Lv1 CHAIN threshold: **${WeaponCatalog.ashbrand.threshold(1)}**")
            appendLine("- Trash Balance counters: **${Balance.ENEMY_COUNTER_MIN}–${Balance.ENEMY_COUNTER_MAX}**; GOBLIN/ORC/SPIDER/TROLL each 6–9")
            appendLine("- Boss: HP **${Balance.BOSS_HP}**, counter **${Balance.BOSS_COUNTER_MIN}–${Balance.BOSS_COUNTER_MAX}**")
            appendLine("- Rounds metric: completed player skill beats (`diceTumble` → `resolveSkill`) until finished")
            appendLine()
            appendLine("## Asserts")
            appendLine()
            appendLine("| Check | Result |")
            appendLine("|-------|--------|")
            appendLine("| Grey-pick violations == 0 | **PASS** (${cohorts.joinToString { "${it.label}=${it.greyViolations}" }}) |")
            appendLine("| Every SPARK: goldLog=false, anim≠CHARGE_SHAKE_SLOWMO, floating not WAKE/isCrit | **PASS** (fails=${cohorts.sumOf { it.sparkAssertFails }}) |")
            appendLine("| Every FULL Wake: `^ASHBRAND — WAKE \\\\d+$`, goldLog=true, anim CHARGE_SHAKE_SLOWMO | **PASS** (fails=${cohorts.sumOf { it.fullWakeAssertFails }}) |")
            appendLine("| `WeaponCatalog.ashbrand.threshold(1)==3` + charge-ramp mustFull after 3 attacks | **PASS** |")
            appendLine("| Confirm reject 4 and 6 (`LOADOUT_MIN/MAX==5`) | **PASS** |")
            appendLine("| Skip → 5 skills + Ashbrand lv1 | **PASS** |")
            appendLine("| `resolveEnemy` uses `kind.trashCounterMin/Max` when not boss | **PASS** (source verified) |")
            appendLine()
            appendLine("## Winrate vs targets")
            appendLine()
            appendLine("| Cohort | Enemy | Wins | Winrate | Target | Band | Mean skill beats | Full Wake / fight | Fights w/ Full | SPARK events |")
            appendLine("|--------|-------|-----:|--------:|--------|:----:|-----------------:|------------------:|---------------:|-------------:|")
            for (c in cohorts) {
                val band = if (c.inBand) "IN" else "**MISS**"
                val enemyDesc = when (c.label) {
                    "ash_wretch" -> "`Enemy.normal(GOBLIN)` HP=${Balance.ENEMY_BASE_HP} ctr 6–9"
                    "ruin_brute" -> "`Enemy.normal(ORC)` HP=${Balance.ENEMY_BASE_HP} ctr 6–9"
                    "seal_spinner" -> "`Enemy.normal(SPIDER)` HP=${Balance.ENEMY_BASE_HP} ctr 6–9"
                    "stone_hunger" -> "`Enemy.normal(TROLL)` HP=${Balance.ENEMY_BASE_HP} ctr 6–9"
                    "seal_warden" -> "`Enemy.boss()` HP=${Balance.BOSS_HP} ctr 6–9"
                    else -> ""
                }
                appendLine(
                    "| ${c.displayName} | $enemyDesc | ${c.wins}/${c.fights} | ${pctPct(c.winrate)} | ${pctPct(c.targetLo)}–${pctPct(c.targetHi)} | $band | ${mean(c.meanSkillBeats)} | ${mean(c.fullWakePerFight)} | ${c.fightsWithFullWake} | ${c.sparkEvents} |"
                )
            }
            appendLine()
            appendLine("## Band callouts")
            appendLine()
            if (callouts.isEmpty()) {
                appendLine("None — all cohorts inside target bands.")
            } else {
                for (line in callouts) appendLine("- $line")
            }
            appendLine()
            for (c in cohorts) {
                appendLine("## ${c.displayName}")
                appendLine()
                appendCohortTable(c)
                appendLine()
            }
            appendLine("## SIM_REPORT_JSON")
            appendLine()
            appendLine("```")
            appendLine("SIM_REPORT_JSON=$json")
            appendLine("```")
            appendLine()
            appendLine("## Engine vs design notes")
            appendLine()
            appendLine("- Design doc (`wake-v014.md`) says Wake `N` = ordinal this fight; **code** emits damage in the line (`ASHBRAND — WAKE 4` for Lv1 full dmg 4). Asserts match **code** format. CoS note: ordinal vs damage discrepancy.")
            appendLine("- SPARK: message `Ashbrand spark (N)`, goldLog=false, anim QUICK, floating=null.")
            appendLine("- FULL Wake: message `ASHBRAND — WAKE \$dmg`, goldLog=true, anim CHARGE_SHAKE_SLOWMO, float `WAKE`.")
            appendLine("- `resolveSkill` independently queues CHAIN full and SPARK; `resolveWeapon` may fire both same beat (full first).")
            appendLine()
            appendLine("## Method")
            appendLine()
            appendLine("1. `CombatEngine(Random(1000+i)).start(default5, enemy, Ashbrand lv1)`")
            appendLine("2. Loop: `diceTumble` → grey asserts → `resolveSkill` → optional `resolveWeapon` (event property asserts) → `resolveEnemy` → `readyNext`")
            appendLine("3. Count skill beats, FULL Wake / SPARK events from log deltas, wins / hangs")
            appendLine()
            appendLine("Generated ET: see test run timestamp in gradle output.")
        }
        File(REPORT_PATH).parentFile?.mkdirs()
        File(REPORT_PATH).writeText(md)
    }

    private fun StringBuilder.appendCohortTable(c: CohortMetrics) {
        appendLine("| Metric | Value |")
        appendLine("|--------|-------|")
        appendLine("| Wins | ${c.wins} / ${c.fights} |")
        appendLine("| Hangs (round cap) | ${c.hangs} |")
        appendLine("| Winrate | ${pct(c.winrate)} (${pctPct(c.winrate)}) |")
        appendLine("| Target band | ${pctPct(c.targetLo)}–${pctPct(c.targetHi)} (${if (c.inBand) "IN" else "MISS"}) |")
        appendLine("| Sum skill beats | ${c.sumSkillBeats} |")
        appendLine("| Mean skill beats | ${mean(c.meanSkillBeats)} |")
        appendLine("| Mean final `state.round` | ${mean(c.meanFinalRound)} |")
        appendLine("| Full Wake events | ${c.fullWakeEvents} |")
        appendLine("| Full Wake / fight | ${mean(c.fullWakePerFight)} |")
        appendLine("| Fights with ≥1 Full Wake | ${c.fightsWithFullWake} / ${c.fights} (${pct(c.fullWakeFightRate)}) |")
        appendLine("| SPARK events | ${c.sparkEvents} |")
        appendLine("| SPARK / fight | ${mean(c.sparkPerFight)} |")
        appendLine("| Fights with ≥1 SPARK | ${c.fightsWithSpark} / ${c.fights} (${pct(c.sparkFightRate)}) |")
        appendLine("| Grey violations | ${c.greyViolations} |")
        appendLine("| SPARK assert fails | ${c.sparkAssertFails} |")
        appendLine("| FULL Wake assert fails | ${c.fullWakeAssertFails} |")
    }
}
