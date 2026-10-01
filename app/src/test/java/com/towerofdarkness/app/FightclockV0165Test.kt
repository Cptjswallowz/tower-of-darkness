package com.towerofdarkness.app

import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.CombatPhase
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.EnemyKits
import com.towerofdarkness.app.domain.combat.FightClock
import com.towerofdarkness.app.domain.combat.FightWhose
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import com.towerofdarkness.app.domain.forge.Forge
import com.towerofdarkness.app.domain.glossary.Glossary
import com.towerofdarkness.app.domain.specials.Specials
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

/**
 * v0.1.65-fightclock P1 — taps, Assist OFF, Brace/Soften fight-long,
 * clock Cycle/Beat/Whose, L/M not forge II/III, vc66.
 */
class FightclockV0165Test {

    private fun gradleSrc(): String {
        val a = File("build.gradle.kts")
        val b = File("app/build.gradle.kts")
        return when {
            a.isFile && a.readText().contains("versionCode") -> a.readText()
            else -> b.readText()
        }
    }

    private fun tank() = Enemy(EnemyKind.ORC, maxHp = 500, hp = 500, isBoss = false)

    private fun specialsLoadout() = listOf(
        "hostflint", "cinder_step", "iron_mantle", "grave_brand", "ash_vow"
    ).mapNotNull { CardCatalog.byId(it) }

    @Test
    fun packaging_vc66_vnFightclock() {
        val g = gradleSrc()
        assertTrue(g.contains("versionCode = 66"))
        assertTrue(g.contains("versionName = \"0.1.65-fightclock\""))
    }

    @Test
    fun glossary_braceSoften_exactArchitect() {
        assertEquals(
            "Absorb damage before HP. Lasts the whole fight or until consumed.",
            Glossary.definition("brace")
        )
        assertEquals(
            "Extra damage taken. Lasts the whole fight.",
            Glossary.definition("soften")
        )
        // No round-end clear copy anywhere in glossary map
        Glossary.terms.values.forEach { body ->
            assertFalse(body.contains("Clears at round end", ignoreCase = true))
            assertFalse(body.contains("round end, clear", ignoreCase = true))
        }
    }

    @Test
    fun brace_notClearedAtRoundEnd() {
        val engine = CombatEngine(Random(1))
        var s = engine.start(
            specialsLoadout(), tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, initialBrace = 4, assistSpecials = false
        )
        assertEquals(4, s.brace)
        s = engine.readyNext(s)
        assertEquals(4, s.brace)
    }

    @Test
    fun soften_persistsAcrossReadyNext() {
        val engine = CombatEngine(Random(2))
        var s = engine.start(
            specialsLoadout(), tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, assistSpecials = false
        )
        s = s.copy(counterPenalty = 3)
        s = engine.readyNext(s)
        assertEquals(3, s.counterPenalty)
    }

    @Test
    fun forge_LM_neverForgeable() {
        assertFalse(Forge.isForgeable(Specials.ID_GRAVE_BRAND))
        assertFalse(Forge.isForgeable(Specials.ID_ASH_VOW))
        val rows = Forge.rows(specialsLoadout(), emptyMap(), 99, 99)
        assertTrue(rows.none { it.cardId == Specials.ID_GRAVE_BRAND })
        assertTrue(rows.none { it.cardId == Specials.ID_ASH_VOW })
        assertTrue(Forge.choicesFor(CardCatalog.byId(Specials.ID_GRAVE_BRAND)!!, Forge.stateOf(emptyMap(), Specials.ID_GRAVE_BRAND)).isEmpty())
        assertTrue(Forge.choicesFor(CardCatalog.byId(Specials.ID_ASH_VOW)!!, Forge.stateOf(emptyMap(), Specials.ID_ASH_VOW)).isEmpty())
    }

    @Test
    fun forge_mantleBodies_noRoundEndClear() {
        val src = File("app/src/main/java/com/towerofdarkness/app/domain/forge/Forge.kt").let {
            if (it.isFile) it else File("src/main/java/com/towerofdarkness/app/domain/forge/Forge.kt")
        }.readText()
        assertFalse(src.contains("If Brace remains at round end, clear it."))
        assertFalse(src.contains("Clears at round end"))
    }

    @Test
    fun clock_cycleBeatWhose_format() {
        val engine = CombatEngine(Random(3))
        var s = engine.start(
            specialsLoadout(), tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, assistSpecials = false
        )
        assertEquals(1, s.regularCycleIndex)
        assertEquals(0, s.cycleBeat)
        val line0 = FightClock.line(s)
        assertTrue(line0.startsWith("Cycle 1 · Beat 0/"))
        assertTrue(line0.endsWith(" · You"))

        s = engine.diceTumble(s)
        s = engine.resolveSkill(s)
        assertEquals(1, s.cycleBeat)
        assertEquals(FightWhose.YOU, s.lastWhose)
        assertTrue(FightClock.line(s).contains("Beat 1/"))
        assertTrue(FightClock.line(s).endsWith(" · You"))

        if (s.awaitingWeapon && !s.awaitingWakeTap) {
            s = engine.resolveWeapon(s, wakeViaAssist = false)
        }
        s = engine.resolveEnemy(s)
        assertEquals(FightWhose.FOE, s.lastWhose)
        assertTrue(FightClock.line(s).endsWith(" · Foe"))

        val y = 3 + EnemyKits.skillsFor(s.enemy).size
        assertEquals(y, FightClock.beatDenom(s))
    }

    @Test
    fun clock_cycleIncrementsOnRegularRefresh() {
        val engine = CombatEngine(Random(4))
        var s = engine.start(
            specialsLoadout(), tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, assistSpecials = false
        )
        repeat(3) {
            s = engine.diceTumble(s)
            s = engine.resolveSkill(s)
            if (s.awaitingWeapon && !s.awaitingWakeTap) {
                s = engine.resolveWeapon(s, wakeViaAssist = false)
            } else if (s.awaitingWakeTap) {
                s = engine.resolveWeapon(s, wakeViaAssist = false)
            }
            if (!s.finished && s.enemy.hp > 0) {
                s = engine.resolveEnemy(s)
                s = engine.readyNext(s)
            }
        }
        s = engine.diceTumble(s)
        assertTrue(s.log.any { it.message == "Cycle reset" })
        assertEquals(2, s.regularCycleIndex)
        assertEquals(0, s.cycleBeat)
        assertTrue(FightClock.line(s).startsWith("Cycle 2"))
    }

    @Test
    fun clock_specialWhose_onGraveBrand() {
        val engine = CombatEngine(Random(5))
        var s = engine.start(
            specialsLoadout(), tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, assistSpecials = false
        )
        s = s.copy(graveBrandCharge = 3)
        s = engine.fireGraveBrand(s, viaAssist = false)
        assertEquals(FightWhose.SPECIAL, s.lastWhose)
        assertTrue(FightClock.line(s).endsWith(" · Special"))
        assertEquals(0, s.graveBrandCharge)
    }

    @Test
    fun brandVow_uiLabels() {
        val engine = CombatEngine(Random(6))
        var s = engine.start(
            specialsLoadout(), tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, assistSpecials = false
        )
        assertEquals("0/3", FightClock.graveLabel(s))
        assertEquals("ready", FightClock.vowLabel(s))
        s = s.copy(graveBrandCharge = 3)
        assertEquals("3/3", FightClock.graveLabel(s))
        s = engine.fireAshVow(s, viaAssist = false)
        assertEquals("spent", FightClock.vowLabel(s))
    }

    @Test
    fun assistOff_neverAutoFiresGraveOrVowOrWake() {
        val engine = CombatEngine(Random(7))
        var s = engine.start(
            specialsLoadout(), tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, assistSpecials = false
        )
        // Charge Grave to 3 via regulars — must NOT auto-fire
        repeat(3) {
            s = engine.diceTumble(s)
            s = engine.resolveSkill(s)
            assertFalse("Assist OFF must not set pendingGraveBrand", s.pendingGraveBrand)
            if (s.awaitingWeapon) {
                // Wake await tap only — resolve without assist auto
                if (s.awaitingWakeTap) {
                    assertTrue(s.pendingFullWake)
                    // Do not fire Wake — Assist OFF waits for tap
                } else {
                    s = engine.resolveWeapon(s, wakeViaAssist = false)
                }
            }
            if (!s.finished && s.enemy.hp > 0 && !s.awaitingWakeTap) {
                s = engine.resolveEnemy(s)
                s = engine.readyNext(s)
            } else if (s.awaitingWakeTap) {
                // Clear wake by tap path explicitly for loop continue
                s = engine.resolveWeapon(s, wakeViaAssist = false)
                if (!s.finished) {
                    s = engine.resolveEnemy(s)
                    s = engine.readyNext(s)
                }
            }
        }
        assertEquals(3, s.graveBrandCharge)
        assertFalse(s.log.any { it.message.contains("Grave Brand (assist)") })
        assertFalse(s.log.any { it.message.contains("Ash Vow (assist)") })

        // Cycle 2 with Assist OFF — Ash Vow must NOT auto
        // Force cycle bump
        val beforeVow = s.ashVowSpent
        s = s.copy(spentIds = Specials.regularIds(s.activeCards), regularCycleIndex = 1)
        s = engine.diceTumble(s)
        assertEquals(2, s.regularCycleIndex)
        assertEquals(beforeVow, s.ashVowSpent)
        assertFalse(s.log.any { it.message == "Ash Vow (assist)" })
    }

    @Test
    fun tap_graveAcceptedWhenReady_ignoredWhenNot() {
        val engine = CombatEngine(Random(8))
        var s = engine.start(
            specialsLoadout(), tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, assistSpecials = false
        )
        // Not ready
        assertTrue(s.graveBrandCharge < 3)
        val ignored = engine.fireGraveBrand(s, viaAssist = false)
        assertEquals(s.graveBrandCharge, ignored.graveBrandCharge)

        s = s.copy(graveBrandCharge = 3, phase = CombatPhase.COMBAT)
        val fired = engine.fireGraveBrand(s, viaAssist = false)
        assertEquals(0, fired.graveBrandCharge)
        assertTrue(fired.log.any { it.message == "Grave Brand (tap)" })
    }

    @Test
    fun tap_vowAcceptedOnce_ignoredWhenSpent() {
        val engine = CombatEngine(Random(9))
        var s = engine.start(
            specialsLoadout(), tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, assistSpecials = false
        )
        s = engine.fireAshVow(s, viaAssist = false)
        assertTrue(s.ashVowSpent)
        val brace = s.brace
        s = engine.fireAshVow(s, viaAssist = false)
        assertEquals(brace, s.brace)
    }

    @Test
    fun tapQueue_semantics_documentedInSource() {
        // Pure unit: GC queue drain order is covered by source presence + engine fire paths.
        val gc = File("app/src/main/java/com/towerofdarkness/app/nav/GameController.kt").let {
            if (it.isFile) it else File("src/main/java/com/towerofdarkness/app/nav/GameController.kt")
        }.readText()
        assertTrue(gc.contains("TAP_SPECIAL"))
        assertTrue(gc.contains("specialTapQueue"))
        assertTrue(gc.contains("drainSpecialTapQueue"))
        assertTrue(gc.contains("combatFxBusy"))
        assertTrue(gc.contains("requestSpecialTap"))
        // accepted|queued|ignored paths
        assertTrue(gc.contains("\"accepted\""))
        assertTrue(gc.contains("\"queued\""))
        assertTrue(gc.contains("\"ignored\""))
    }

    @Test
    fun wakeDots_matchAshbrandPips() {
        val engine = CombatEngine(Random(10))
        var s = engine.start(
            specialsLoadout(), tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99
        )
        val th = s.weapon.threshold
        assertEquals("○".repeat(th), FightClock.wakeDots(s))
        s = s.copy(weapon = s.weapon.copy(charge = th))
        assertEquals("●".repeat(th), FightClock.wakeDots(s))
    }
}
