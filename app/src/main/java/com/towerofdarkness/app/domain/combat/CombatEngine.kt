package com.towerofdarkness.app.domain.combat

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.domain.cards.Card
import com.towerofdarkness.app.domain.effects.Equipment
import com.towerofdarkness.app.domain.effects.MoveEffect
import com.towerofdarkness.app.domain.effects.SkillEffect
import kotlin.random.Random

enum class CombatAnimStyle { QUICK, CHARGE_SHAKE_SLOWMO }

data class FloatingText(val text: String, val isPlayer: Boolean, val isCrit: Boolean = false)

data class CombatEvent(
    val message: String,
    val floating: FloatingText? = null,
    val animStyle: CombatAnimStyle = CombatAnimStyle.QUICK,
    val sound: String = "hit"
)

data class CombatState(
    val playerHp: Int,
    val playerMaxHp: Int,
    val brace: Int = 0,
    val enemy: Enemy,
    val activeCards: List<Card>,
    val round: Int = 1,
    val log: List<CombatEvent> = emptyList(),
    val counterPenalty: Int = 0,
    val finished: Boolean = false,
    val playerWon: Boolean = false,
    val lastFiredCard: Card? = null
)

class CombatEngine(private val rng: Random = Random.Default) {

    fun start(activeCards: List<Card>, enemy: Enemy, maxHp: Int = Balance.PLAYER_MAX_HP): CombatState =
        CombatState(playerHp = maxHp, playerMaxHp = maxHp, enemy = enemy, activeCards = activeCards)

    fun step(state: CombatState): CombatState {
        if (state.finished) return state
        var s = state
        val events = mutableListOf<CombatEvent>()
        events += CombatEvent("Dice tumble…", sound = "dice")

        val card = pickWeighted(s.activeCards) ?: return s
        s = s.copy(lastFiredCard = card)
        val anim = if (card.rarity == Rarity.RARE || card.rarity == Rarity.LEGENDARY)
            CombatAnimStyle.CHARGE_SHAKE_SLOWMO else CombatAnimStyle.QUICK

        val (afterCard, cardEvents) = resolveCard(s, card, anim)
        s = afterCard
        events += cardEvents
        // Brace clears at round end if unused leftover from equipment-only — keep until enemy hit
        if (s.enemy.hp <= 0) {
            events += CombatEvent("Victory!", FloatingText("WIN", true, true), anim, "legendary")
            return s.copy(log = s.log + events, finished = true, playerWon = true)
        }

        // Enemy counter
        val (cMin, cMax) = if (s.enemy.isBoss)
            Balance.BOSS_COUNTER_MIN to Balance.BOSS_COUNTER_MAX
        else
            Balance.ENEMY_COUNTER_MIN to Balance.ENEMY_COUNTER_MAX
        var dmg = rng.nextInt(cMin, cMax + 1) - s.counterPenalty
        dmg = dmg.coerceAtLeast(1)

        var brace = s.brace
        var absorbed = 0
        if (brace > 0) {
            absorbed = minOf(brace, dmg)
            brace -= absorbed
            dmg -= absorbed
        }
        val newHp = (s.playerHp - dmg).coerceAtLeast(0)
        val msg = buildString {
            append("${s.enemy.kind.displayName} counters for $dmg")
            if (absorbed > 0) append(" ($absorbed braced)")
        }
        events += CombatEvent(msg, FloatingText("-$dmg", false), sound = "hit")

        // Clear leftover brace at end of round (Iron Mantle rule)
        brace = 0

        if (newHp <= 0) {
            events += CombatEvent("Defeat…", FloatingText("DOWN", false), sound = "miss")
            return s.copy(
                playerHp = 0, brace = 0, counterPenalty = 0,
                log = s.log + events, finished = true, playerWon = false, round = s.round + 1
            )
        }
        return s.copy(
            playerHp = newHp, brace = brace, counterPenalty = 0,
            log = s.log + events, round = s.round + 1
        )
    }

    private fun resolveCard(
        state: CombatState, card: Card, anim: CombatAnimStyle
    ): Pair<CombatState, List<CombatEvent>> {
        val events = mutableListOf<CombatEvent>()
        var s = state
        var enemy = s.enemy
        val sound = if (card.rarity == Rarity.RARE) "legendary" else "card_fire"

        when (val e = card.effect) {
            is SkillEffect.Damage -> {
                enemy = enemy.copy(hp = (enemy.hp - e.damage).coerceAtLeast(0))
                events += CombatEvent("${card.title} deals ${e.damage}", FloatingText("-${e.damage}", true, card.rarity == Rarity.RARE), anim, sound)
                s = s.copy(enemy = enemy)
            }
            is SkillEffect.DamageAndHeal -> {
                enemy = enemy.copy(hp = (enemy.hp - e.damage).coerceAtLeast(0))
                val nh = (s.playerHp + e.heal).coerceAtMost(s.playerMaxHp)
                events += CombatEvent("${card.title}: ${e.damage} dmg, +${e.heal} HP", FloatingText("-${e.damage}", true), anim, sound)
                s = s.copy(enemy = enemy, playerHp = nh)
            }
            is MoveEffect.DamageAndSoften -> {
                enemy = enemy.copy(hp = (enemy.hp - e.damage).coerceAtLeast(0))
                var brace = s.brace + e.braceGain
                events += CombatEvent("${card.title} deals ${e.damage}", FloatingText("-${e.damage}", true), anim, sound)
                if (e.braceGain > 0) events += CombatEvent("Brace +${e.braceGain}", FloatingText("BRACE", true), anim, sound)
                if (e.counterPenalty > 0) events += CombatEvent("Counter softened −${e.counterPenalty}", sound = "ui")
                s = s.copy(
                    enemy = enemy,
                    brace = brace,
                    counterPenalty = s.counterPenalty + e.counterPenalty
                )
            }
            is Equipment.GainBrace -> {
                s = s.copy(brace = s.brace + e.brace)
                events += CombatEvent("${card.title}: Brace ${e.brace}", FloatingText("BRACE ${e.brace}", true), anim, sound)
            }
            is Equipment.HealOrBrace -> {
                if (s.playerHp >= s.playerMaxHp) {
                    s = s.copy(brace = s.brace + e.braceIfFull)
                    events += CombatEvent("${card.title}: Brace ${e.braceIfFull}", FloatingText("BRACE", true), anim, sound)
                } else {
                    val nh = (s.playerHp + e.heal).coerceAtMost(s.playerMaxHp)
                    s = s.copy(playerHp = nh)
                    events += CombatEvent("${card.title}: +${e.heal} HP", FloatingText("+${e.heal}", true), anim, sound)
                }
            }
        }
        return s to events
    }

    private fun pickWeighted(cards: List<Card>): Card? {
        if (cards.isEmpty()) return null
        val total = cards.sumOf { it.weight }
        var r = rng.nextInt(total)
        for (c in cards) {
            r -= c.weight
            if (r < 0) return c
        }
        return cards.last()
    }
}
