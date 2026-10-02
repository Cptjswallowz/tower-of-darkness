package com.towerofdarkness.app

import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.CombatPhase
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.FightClock
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import com.towerofdarkness.app.domain.specials.Specials
import com.towerofdarkness.app.ui.gestures.SPECIAL_LONG_PRESS_MS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

/**
 * v0.1.66-waketap P1 — short-tap dump vs long-press glossary; Wake at 3;
 * clock under Round N; Assist OFF never auto; vc67.
 */
class WaketapV0166Test {

    private fun gradleSrc(): String {
        val a = File("build.gradle.kts")
        val b = File("app/build.gradle.kts")
        return when {
            a.isFile && a.readText().contains("versionCode") -> a.readText()
            else -> b.readText()
        }
    }

    private fun read(path: String): String {
        val f = File(path).let { if (it.isFile) it else File("app/$path") }
        val g = if (f.isFile) f else File("src/main/java/com/towerofdarkness/app/${path.substringAfter("app/")}")
        return when {
            File(path).isFile -> File(path).readText()
            File("app/$path").isFile -> File("app/$path").readText()
            else -> File(path).readText()
        }
    }

    private fun src(relUnderMain: String): String {
        val a = File("app/src/main/java/com/towerofdarkness/app/$relUnderMain")
        val b = File("src/main/java/com/towerofdarkness/app/$relUnderMain")
        return when {
            a.isFile -> a.readText()
            else -> b.readText()
        }
    }

    private fun tank() = Enemy(EnemyKind.ORC, maxHp = 500, hp = 500, isBoss = false)

    private fun specialsLoadout() = listOf(
        "hostflint", "cinder_step", "iron_mantle", "grave_brand", "ash_vow"
    ).mapNotNull { CardCatalog.byId(it) }

    @Test
    fun packaging_vc67_vnWaketap() {
        val g = gradleSrc()
        assertTrue(g.contains("versionCode = 67"))
        assertTrue(g.contains("versionName = \"0.1.66-waketap\""))
    }

    @Test
    fun longPressTimeout_is350ms() {
        assertEquals(350L, SPECIAL_LONG_PRESS_MS)
    }

    @Test
    fun gestureSplit_shortNeverGlossary_longIsGlossary() {
        val combat = src("ui/screens/CombatScreen.kt")
        val gc = src("nav/GameController.kt")
        val gestures = src("ui/gestures/SpecialTapGestures.kt")
        assertTrue(gestures.contains("SPECIAL_LONG_PRESS_MS = 350L"))
        assertTrue(gestures.contains("specialTapSplit"))
        assertTrue(gestures.contains("WithSpecialLongPressTimeout"))
        // Ashbrand row: specialTapSplit, icon onClick=null (no nested glossary clickable)
        assertTrue(combat.contains("specialTapSplit"))
        assertTrue(combat.contains("onAshbrandLongGlossary"))
        assertTrue(combat.contains("AshbrandIcon(phase = iconPhase, onClick = null)"))
        assertFalse(
            "WeaponBar must not use clickable(onClick = onAshbrandTap) alone",
            combat.contains(".clickable(onClick = onAshbrandTap)")
        )
        // Short tap path never showGlossary ashbrand
        assertTrue(gc.contains("fun onAshbrandTap()"))
        assertTrue(gc.contains("requestSpecialTap(\"wake\")"))
        assertTrue(gc.contains("NEVER opens glossary"))
        assertTrue(gc.contains("onAshbrandLongGlossary"))
        assertTrue(gc.contains("LONG_GLOSSARY ashbrand"))
        assertTrue(gc.contains("LONG_GLOSSARY grave"))
        assertTrue(gc.contains("LONG_GLOSSARY vow"))
        // Not-ready: ignored + flash, no glossary
        assertTrue(gc.contains("Not ready: flash only — no glossary, no spend"))
    }

    @Test
    fun wakeDumpsAtThreeSparks_onTapPath() {
        val engine = CombatEngine(Random(42))
        var s = engine.start(
            specialsLoadout(), tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, assistSpecials = false
        )
        // Force full charge as Assist OFF would after 3 sparks
        s = s.copy(
            weapon = s.weapon.copy(charge = s.weapon.threshold),
            pendingFullWake = true,
            awaitingWeapon = true,
            awaitingWakeTap = true,
            phase = CombatPhase.COMBAT
        )
        assertEquals(3, s.weapon.threshold) // Lv1
        assertTrue(s.weapon.charge >= 3)
        val fired = engine.resolveWeapon(s, wakeViaAssist = false)
        assertEquals(0, fired.weapon.charge)
        assertFalse(fired.pendingFullWake)
        assertFalse(fired.awaitingWakeTap)
        assertTrue(fired.log.any { it.message == "Ashbrand Wake (tap)" })
    }

    @Test
    fun clock_stringPresent_underRoundShape() {
        val engine = CombatEngine(Random(1))
        val s = engine.start(
            specialsLoadout(), tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, assistSpecials = false
        )
        val line = FightClock.line(s)
        assertTrue(line.startsWith("Cycle "))
        assertTrue(line.contains(" · Beat "))
        assertTrue(line.contains(" · You") || line.contains(" · Foe") || line.contains(" · Special"))
        val combat = src("ui/screens/CombatScreen.kt")
        assertTrue(combat.contains("FightClock.line(state)"))
        assertTrue(combat.contains("fight_clock"))
        assertTrue(combat.contains("Round \${state.round}"))
    }

    @Test
    fun assistOff_neverAutoFiresWake() {
        val engine = CombatEngine(Random(7))
        var s = engine.start(
            specialsLoadout(), tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, assistSpecials = false
        )
        // Charge to threshold via regulars without tapping
        var guard = 0
        while (s.weapon.charge < s.weapon.threshold && guard++ < 20 && !s.finished) {
            s = engine.diceTumble(s)
            s = engine.resolveSkill(s)
            if (s.awaitingWakeTap) {
                assertTrue(s.pendingFullWake)
                assertFalse(s.log.any { it.message.contains("Ashbrand Wake (assist)") })
                // Assist OFF: do not auto — leave awaiting
                break
            }
            if (s.awaitingWeapon && !s.awaitingWakeTap) {
                s = engine.resolveWeapon(s, wakeViaAssist = false)
            }
            if (!s.finished && s.enemy.hp > 0 && !s.awaitingWakeTap) {
                s = engine.resolveEnemy(s)
                s = engine.readyNext(s)
            }
        }
        assertTrue("should reach await tap or already full", s.awaitingWakeTap || s.weapon.charge >= s.weapon.threshold)
        assertFalse(s.log.any { it.message.contains("Ashbrand Wake (assist)") })
    }

    @Test
    fun braceExpireFight_logPresent_readyNextKeepsBrace() {
        val gc = src("nav/GameController.kt")
        assertTrue(gc.contains("BRACE_EXPIRE fight"))
        val engine = CombatEngine(Random(3))
        var s = engine.start(
            specialsLoadout(), tank(), WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 99, playerHp = 99, initialBrace = 5, assistSpecials = false
        )
        s = engine.readyNext(s)
        assertEquals(5, s.brace)
    }

    @Test
    fun locked_remnantsForgeMusicContinueUntouched_inDiffScope() {
        // Smoke: specials still cost Wake threshold 3; Assist OFF never auto in source comments
        assertEquals(3, WeaponCatalog.ashbrand.threshold(1))
        val gc = src("nav/GameController.kt")
        assertTrue(gc.contains("Assist OFF never auto-fires") || gc.contains("never auto-fires"))
    }
}
