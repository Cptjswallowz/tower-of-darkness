package com.towerofdarkness.app

import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatBeat
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.WeaponCatalog
import com.towerofdarkness.app.domain.combat.WeaponRuntime
import com.towerofdarkness.app.nav.GameController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class WeaponXpTest {

    @Test
    fun nextWeaponLevel_fullWakeWin_levelsUp() {
        assertEquals(2, GameController.nextWeaponLevelAfterFight(1, true, false, true))
        assertEquals(3, GameController.nextWeaponLevelAfterFight(2, true, false, true))
        assertEquals(3, GameController.nextWeaponLevelAfterFight(3, true, false, true))
    }

    @Test
    fun nextWeaponLevel_noWake_noLevel() {
        assertEquals(1, GameController.nextWeaponLevelAfterFight(1, false, false, true))
    }

    @Test
    fun nextWeaponLevel_loss_noLevel() {
        assertEquals(1, GameController.nextWeaponLevelAfterFight(1, true, false, false))
    }

    @Test
    fun spinnerWin_fullWake_ashbrandBecomesLv2_beforeNextNode() {
        val cards = CardCatalog.defaultLoadoutIds.mapNotNull { CardCatalog.byId(it) }
        val engine = CombatEngine(Random(42))
        var s = engine.start(
            cards,
            Enemy(EnemyKind.SPIDER, maxHp = 20, hp = 20, isBoss = false),
            WeaponRuntime(WeaponCatalog.ashbrand, level = 1, charge = 3),
            maxHp = 30,
            playerHp = 30
        )
        s = s.copy(awaitingWeapon = true, pendingFullWake = true, pendingSpark = false)
        s = engine.resolveWeapon(s)
        assertTrue(s.fullProcThisCombat)
        var guard = 0
        while (s.enemy.hp > 0 && !s.finished && guard < 40) {
            s = engine.diceTumble(s)
            if (s.highlightedId == null) break
            s = engine.resolveSkill(s)
            if (s.awaitingWeapon) s = engine.resolveWeapon(s)
            if (s.finished) break
            if (s.enemy.hp <= 0) {
                s = s.copy(finished = true, playerWon = true, beat = CombatBeat.AWAITING_CONTINUE)
                break
            }
            s = engine.resolveEnemy(s)
            if (!s.finished) s = engine.readyNext(s)
            guard++
        }
        val won = s.playerWon || (s.enemy.hp <= 0 && s.playerHp > 0)
        assertTrue("spinner should die", won)
        val nextLv = GameController.nextWeaponLevelAfterFight(
            startLevel = 1,
            fullProcThisCombat = s.fullProcThisCombat,
            fullProcKilled = s.fullProcKilled,
            playerWon = won
        )
        assertEquals("Ashbrand must be Lv2 before Warden", 2, nextLv)
    }
}
