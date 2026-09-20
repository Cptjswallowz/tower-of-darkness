package com.towerofdarkness.app

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.cards.CardCatalog
import com.towerofdarkness.app.domain.combat.CombatEngine
import com.towerofdarkness.app.domain.combat.Enemy
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CombatEngineTest {
    @Test
    fun bossHasConfiguredHp() {
        val boss = Enemy.boss()
        assertTrue(boss.maxHp == Balance.BOSS_HP)
        assertTrue(boss.isBoss)
    }

    @Test
    fun fightTerminates() {
        val cards = CardCatalog.defaultLoadoutIds.mapNotNull { CardCatalog.byId(it) }
        val engine = CombatEngine(Random(42))
        var s = engine.start(cards, Enemy.normal())
        var guard = 0
        while (!s.finished && guard < 40) {
            s = engine.step(s)
            guard++
        }
        assertTrue("fight should end", s.finished)
        assertTrue(guard < 40)
    }
}
