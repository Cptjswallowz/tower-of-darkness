package com.towerofdarkness.app

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatBeat
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.CombatPhase
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
 * v0.1.61-continue — VICTORY phase + Continue + stone-under-Ashbrand plate fix.
 *
 * Stone painter that expanded into log well: [SharedTilePlateBox] in
 * `ui/components/TilePlateBackdrop.kt` (fillMaxSize → matchParentSize).
 */
class ContinueV0161Test {

    private fun fiveCards() = listOf("hostflint", "cinder_step", "iron_mantle", "emberbrand", "dust_veil").mapNotNull { CardCatalog.byId(it) }

    private fun combatSrc() =
        File("src/main/java/com/towerofdarkness/app/ui/screens/CombatScreen.kt").readText()

    private fun gcSrc() =
        File("src/main/java/com/towerofdarkness/app/nav/GameController.kt").readText()

    private fun plateSrc() =
        File("src/main/java/com/towerofdarkness/app/ui/components/TilePlateBackdrop.kt").readText()

    private fun engineSrc() =
        File("src/main/java/com/towerofdarkness/app/domain/combat/CombatEngine.kt").readText()

    private fun sealWardenAtZero(youHp: Int = 22) = Enemy(
        kind = EnemyKind.DRAGON,
        maxHp = 28,
        hp = 0,
        isBoss = true
    )

    @Test
    fun winOnFoe0_setsVictoryPhase_andContinueBinding() {
        val engine = CombatEngine(Random(1))
        var s = engine.start(
            activeCards = fiveCards(),
            enemy = sealWardenAtZero(),
            weapon = WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 30,
            playerHp = 30
        )
        s = engine.applyWinCheck(s)
        assertEquals(CombatPhase.VICTORY, s.phase)
        assertTrue(s.finished)
        assertTrue(s.playerWon)
        assertEquals(CombatBeat.AWAITING_CONTINUE, s.beat)
        assertTrue(s.log.any { it.message == "Victory" || it.message.startsWith("Victory") })

        val combat = combatSrc()
        assertTrue(combat.contains("CombatPhase.VICTORY"))
        assertTrue(combat.contains("gc.continueAfterCombat()"))
        assertTrue(combat.contains("ButtonDefaults.buttonColors"))
        assertTrue(combat.contains("containerColor = Accent"))
        assertTrue("Flee hidden on combat end", combat.contains("if (combatEnded)"))
        assertFalse(
            "Continue must not be a log-line click",
            combat.substringAfter("Combat log well UNDER Ashbrand")
                .substringBefore("// v0.1.61-continue: Continue is OWN button")
                .contains("continueAfterCombat")
        )
    }

    @Test
    fun failsafe400ms_constant_andUiHook() {
        assertEquals(400L, Balance.COMBAT_END_FORCE_MS)
        val combat = combatSrc()
        assertTrue(combat.contains("forceVictoryFromUi"))
        assertTrue(combat.contains("Balance.COMBAT_END_FORCE_MS"))
        val gc = gcSrc()
        assertTrue(gc.contains("fun forceVictoryFromUi"))
        assertTrue(gc.contains("COMBAT_END win foe="))
        assertTrue(gc.contains("phase=\${"))
    }

    @Test
    fun noPlateUnderLogWell_matchParentSizeOnPlateBox() {
        val plate = plateSrc()
        assertTrue("stone-file must keep SharedTilePlateBox", plate.contains("fun SharedTilePlateBox"))
        assertTrue(
            "WeaponBar plate must not expand into log well — matchParentSize",
            plate.contains("matchParentSize()")
        )
        val imageBlock = plate.substringAfter("painterResource(R.drawable.ui_tile_plate)")
            .substringBefore("content()")
        assertFalse(
            "Image under SharedTilePlateBox must not use fillMaxSize (expands WeaponBar)",
            imageBlock.contains("fillMaxSize()")
        )
        assertTrue(imageBlock.contains("matchParentSize()"))

        val combat = combatSrc()
        assertTrue(combat.contains("Combat log well UNDER Ashbrand"))
        val logWell = combat.substringAfter("Combat log well UNDER Ashbrand")
            .substringBefore("// v0.1.61-continue: Continue is OWN button")
        assertFalse("Log well must not call SharedTilePlateBox(", logWell.contains("SharedTilePlateBox("))
        assertFalse("Log well must not load painterResource plate", logWell.contains("painterResource"))
        assertTrue("Log must keep takeLast for ≥3 lines", logWell.contains("takeLast(6)"))
    }

    @Test
    fun packaging_vc63_vnScore() {
        val g = File("build.gradle.kts").readText()
        assertTrue(g.contains("versionCode = 65"))
        assertTrue(g.contains("versionName = \"0.1.64-specials\""))
    }

    @Test
    fun winCheckFunction_presentInEngine() {
        val eng = engineSrc()
        assertTrue(eng.contains("fun applyWinCheck"))
        assertTrue(eng.contains("fun forceVictory"))
        assertTrue(eng.contains("enum class CombatPhase"))
        assertTrue(eng.contains("state.enemy.hp <= 0 && state.phase != CombatPhase.DEFEAT"))
    }

    @Test
    fun forceVictory_fromZeroHpBoss() {
        val engine = CombatEngine(Random(42))
        val s0 = engine.start(
            activeCards = fiveCards(),
            enemy = sealWardenAtZero(youHp = 22),
            weapon = WeaponRuntime(WeaponCatalog.ashbrand),
            maxHp = 30,
            playerHp = 22
        )
        assertEquals(CombatPhase.COMBAT, s0.phase)
        val s1 = engine.forceVictory(s0)
        assertEquals(CombatPhase.VICTORY, s1.phase)
        assertTrue(s1.finished)
        assertTrue(s1.playerWon)
        assertEquals(0, s1.enemy.hp)
        assertEquals("Victory", s1.log.last().message)
    }
}
