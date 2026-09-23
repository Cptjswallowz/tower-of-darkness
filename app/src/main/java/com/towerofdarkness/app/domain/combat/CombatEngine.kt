package com.towerofdarkness.app.domain.combat

import com.towerofdarkness.app.domain.Balance
import com.towerofdarkness.app.domain.Rarity
import com.towerofdarkness.app.domain.cards.Card
import com.towerofdarkness.app.domain.effects.Equipment
import com.towerofdarkness.app.domain.effects.MoveEffect
import com.towerofdarkness.app.domain.effects.SkillEffect
import kotlin.random.Random

enum class CombatAnimStyle { QUICK, CHARGE_SHAKE_SLOWMO }

enum class CombatBeat {
    READY,
    AFTER_DICE,
    AFTER_SKILL,
    AFTER_WEAPON,
    AFTER_ENEMY,
    AWAITING_CONTINUE
}

data class FloatingText(val text: String, val isPlayer: Boolean, val isCrit: Boolean = false)

data class CombatEvent(
    val message: String,
    val floating: FloatingText? = null,
    val animStyle: CombatAnimStyle = CombatAnimStyle.QUICK,
    val sound: String = "hit",
    val glossaryHints: List<String> = emptyList(),
    val goldLog: Boolean = false
)

data class CombatState(
    val playerHp: Int,
    val playerMaxHp: Int,
    val brace: Int = 0,
    val enemy: Enemy,
    val activeCards: List<Card>,
    val spentIds: Set<String> = emptySet(),
    val highlightedId: String? = null,
    val weapon: WeaponRuntime,
    val round: Int = 1,
    val log: List<CombatEvent> = emptyList(),
    val counterPenalty: Int = 0,
    val finished: Boolean = false,
    val playerWon: Boolean = false,
    val lastFiredCard: Card? = null,
    val beat: CombatBeat = CombatBeat.READY,
    val weaponFlashed: Boolean = false,
    val fullProcThisCombat: Boolean = false,
    val fullProcKilled: Boolean = false,
    val lastSkillWasAttack: Boolean = false,
    val awaitingWeapon: Boolean = false,
    /** Queued independently — both may fire same beat. */
    val pendingFullWake: Boolean = false,
    val pendingSpark: Boolean = false,
    /** Sticky FULL Wake line for UI last-5 pin during Wake hold. */
    val pinnedWakeLine: String? = null
)

class CombatEngine(private val rng: Random = Random.Default) {

    fun start(
        activeCards: List<Card>,
        enemy: Enemy,
        weapon: WeaponRuntime,
        maxHp: Int = Balance.PLAYER_MAX_HP,
        playerHp: Int = maxHp
    ): CombatState = CombatState(
        playerHp = playerHp.coerceIn(1, maxHp),
        playerMaxHp = maxHp,
        enemy = enemy,
        activeCards = activeCards,
        weapon = weapon
    )

    /** A — dice tumble: reset cycle if needed, pick unspent weighted slot. */
    fun diceTumble(state: CombatState): CombatState {
        if (state.finished || state.beat == CombatBeat.AWAITING_CONTINUE) return state
        var spent = state.spentIds
        val events = mutableListOf<CombatEvent>()
        if (spent.size >= state.activeCards.size && state.activeCards.isNotEmpty()) {
            spent = emptySet()
            events += CombatEvent("Cycle reset", sound = "ui")
        }
        events += CombatEvent("Dice tumble…", sound = "dice")
        val live = state.activeCards.filter { it.id !in spent }
        val card = pickWeighted(live) ?: return state.copy(log = state.log + events)
        return state.copy(
            spentIds = spent,
            highlightedId = card.id,
            lastFiredCard = card,
            log = state.log + events,
            beat = CombatBeat.AFTER_DICE,
            weaponFlashed = false,
            pinnedWakeLine = null
        )
    }

    /** C — resolve highlighted skill; mark spent; attack skills +1 weapon charge. */
    fun resolveSkill(state: CombatState): CombatState {
        val card = state.lastFiredCard ?: return state
        val anim = if (card.rarity == Rarity.RARE || card.rarity == Rarity.LEGENDARY)
            CombatAnimStyle.CHARGE_SHAKE_SLOWMO else CombatAnimStyle.QUICK
        val (after, events, isAttack) = resolveCard(state, card, anim)
        var s = after.copy(
            spentIds = state.spentIds + card.id,
            highlightedId = card.id,
            lastSkillWasAttack = isAttack,
            log = state.log + events,
            beat = CombatBeat.AFTER_SKILL
        )
        if (isAttack) {
            val w = s.weapon
            s = s.copy(weapon = w.copy(charge = (w.charge + 1).coerceAtMost(w.threshold + 2)))
        }
        // CHAIN full and SPARK are independent — both may queue same beat
        val mustFull = s.weapon.charge >= s.weapon.threshold
        val sparkChance = 0.08f + 0.04f * s.weapon.level
        val spark = rng.nextFloat() < sparkChance
        s = s.copy(
            awaitingWeapon = mustFull || spark,
            pendingFullWake = mustFull,
            pendingSpark = spark
        )
        if (s.enemy.hp <= 0 && !s.awaitingWeapon) {
            return finishVictory(s)
        }
        return s
    }

    /**
     * E — weapon beat. CHAIN full Wake and SPARK may both fire the same beat:
     * full Wake resets charge to 0; SPARK deals half and does not touch charge
     * (including after a same-beat reset).
     */
    fun resolveWeapon(state: CombatState): CombatState {
        if (!state.awaitingWeapon && !state.pendingFullWake && !state.pendingSpark) {
            return state.copy(
                beat = CombatBeat.AFTER_WEAPON,
                awaitingWeapon = false,
                pendingFullWake = false,
                pendingSpark = false
            )
        }
        var s = state
        var w = s.weapon
        val events = mutableListOf<CombatEvent>()
        var fullProc = s.fullProcThisCombat
        var fullKill = s.fullProcKilled
        val doFull = state.pendingFullWake || w.charge >= w.threshold
        val doSpark = state.pendingSpark

        var pinned: String? = s.pinnedWakeLine
        if (doFull) {
            val dmg = w.def.fullDmg(w.level)
            val enemy = s.enemy.copy(hp = (s.enemy.hp - dmg).coerceAtLeast(0))
            // Exact CHAIN FULL Wake payoff line (gold + WAKE float + legendary anim)
            val wakeLine = "ASHBRAND — WAKE $dmg"
            events += CombatEvent(
                message = wakeLine,
                floating = FloatingText("WAKE", true, true),
                animStyle = CombatAnimStyle.CHARGE_SHAKE_SLOWMO,
                sound = "legendary",
                goldLog = true
            )
            pinned = wakeLine
            fullProc = true
            if (enemy.hp <= 0) fullKill = true
            w = w.copy(charge = 0)
            s = s.copy(enemy = enemy, weapon = w, fullProcThisCombat = fullProc, fullProcKilled = fullKill)
        }

        if (doSpark) {
            val dmg = w.def.sparkDmg(w.level)
            val enemy = s.enemy.copy(hp = (s.enemy.hp - dmg).coerceAtLeast(0))
            // Spark: plain log only — no anim, no gold, no WAKE float
            events += CombatEvent(
                message = "Ashbrand spark ($dmg)",
                floating = null,
                animStyle = CombatAnimStyle.QUICK,
                sound = "card_fire",
                goldLog = false
            )
            s = s.copy(enemy = enemy, weapon = w)
        }

        s = s.copy(
            weaponFlashed = doFull, // gold slam only on FULL Wake
            log = s.log + events,
            awaitingWeapon = false,
            pendingFullWake = false,
            pendingSpark = false,
            pinnedWakeLine = pinned,
            beat = CombatBeat.AFTER_WEAPON
        )
        if (s.enemy.hp <= 0) return finishVictory(s)
        return s
    }

    /** F — enemy counter (skipped if already dead). */
    fun resolveEnemy(state: CombatState): CombatState {
        if (state.enemy.hp <= 0) return finishVictory(state)
        val events = mutableListOf<CombatEvent>()
        events += CombatEvent("${state.enemy.kind.displayName} winds up…", sound = "ui")
        val (cMin, cMax) = if (state.enemy.isBoss)
            Balance.BOSS_COUNTER_MIN to Balance.BOSS_COUNTER_MAX
        else
            state.enemy.kind.trashCounterMin to state.enemy.kind.trashCounterMax
        var dmg = rng.nextInt(cMin, cMax + 1) - state.counterPenalty
        dmg = dmg.coerceAtLeast(1)
        var brace = state.brace
        var absorbed = 0
        if (brace > 0) {
            absorbed = minOf(brace, dmg)
            brace -= absorbed
            dmg -= absorbed
        }
        val newHp = (state.playerHp - dmg).coerceAtLeast(0)
        val msg = buildString {
            append("${state.enemy.kind.displayName} hits for $dmg")
            if (absorbed > 0) append(" ($absorbed Brace)")
        }
        events += CombatEvent(
            msg,
            FloatingText("-$dmg", false),
            sound = "hit",
            glossaryHints = if (absorbed > 0) listOf("brace") else emptyList()
        )
        if (newHp <= 0) {
            events += CombatEvent("Defeat…", FloatingText("DOWN", false), sound = "miss")
            return state.copy(
                playerHp = 0,
                brace = 0,
                counterPenalty = 0,
                log = state.log + events,
                finished = true,
                playerWon = false,
                highlightedId = null,
                beat = CombatBeat.AWAITING_CONTINUE,
                round = state.round + 1
            )
        }
        // Soften consumed this counter; Brace leftover remains until readyNext (round end).
        return state.copy(
            playerHp = newHp,
            brace = brace,
            counterPenalty = 0,
            log = state.log + events,
            highlightedId = null,
            beat = CombatBeat.AFTER_ENEMY,
            round = state.round + 1
        )
    }

    /** Advance to next round — clear unused Brace leftover per cards-v0. */
    fun readyNext(state: CombatState): CombatState =
        state.copy(beat = CombatBeat.READY, weaponFlashed = false, brace = 0)

    private fun finishVictory(state: CombatState): CombatState {
        val events = listOf(
            CombatEvent("Victory!", FloatingText("WIN", true, true), CombatAnimStyle.CHARGE_SHAKE_SLOWMO, "legendary")
        )
        return state.copy(
            log = state.log + events,
            finished = true,
            playerWon = true,
            highlightedId = null,
            awaitingWeapon = false,
            beat = CombatBeat.AWAITING_CONTINUE
        )
    }

    private fun resolveCard(
        state: CombatState, card: Card, anim: CombatAnimStyle
    ): Triple<CombatState, List<CombatEvent>, Boolean> {
        val events = mutableListOf<CombatEvent>()
        var s = state
        var enemy = s.enemy
        val sound = if (card.rarity == Rarity.RARE) "legendary" else "card_fire"
        var isAttack = false

        when (val e = card.effect) {
            is SkillEffect.Damage -> {
                isAttack = true
                enemy = enemy.copy(hp = (enemy.hp - e.damage).coerceAtLeast(0))
                events += CombatEvent(
                    "${card.title} deals ${e.damage}",
                    FloatingText("-${e.damage}", true, card.rarity == Rarity.RARE),
                    anim, sound
                )
                s = s.copy(enemy = enemy)
            }
            is SkillEffect.DamageAndHeal -> {
                isAttack = true
                enemy = enemy.copy(hp = (enemy.hp - e.damage).coerceAtLeast(0))
                val nh = (s.playerHp + e.heal).coerceAtMost(s.playerMaxHp)
                events += CombatEvent(
                    "${card.title}: ${e.damage} dmg, +${e.heal} HP",
                    FloatingText("-${e.damage}", true), anim, sound
                )
                s = s.copy(enemy = enemy, playerHp = nh)
            }
            is MoveEffect.DamageAndSoften -> {
                isAttack = true
                enemy = enemy.copy(hp = (enemy.hp - e.damage).coerceAtLeast(0))
                val brace = s.brace + e.braceGain
                events += CombatEvent(
                    "${card.title} deals ${e.damage}",
                    FloatingText("-${e.damage}", true), anim, sound
                )
                if (e.braceGain > 0) {
                    events += CombatEvent(
                        "Brace +${e.braceGain}",
                        FloatingText("BRACE", true), anim, sound,
                        glossaryHints = listOf("brace")
                    )
                }
                if (e.counterPenalty > 0) {
                    events += CombatEvent(
                        "Counter softened −${e.counterPenalty}",
                        sound = "ui",
                        glossaryHints = listOf("soften")
                    )
                }
                s = s.copy(enemy = enemy, brace = brace, counterPenalty = s.counterPenalty + e.counterPenalty)
            }
            is Equipment.GainBrace -> {
                isAttack = false
                s = s.copy(brace = s.brace + e.brace)
                events += CombatEvent(
                    "${card.title}: Brace ${e.brace}",
                    FloatingText("BRACE ${e.brace}", true), anim, sound,
                    glossaryHints = listOf("brace")
                )
            }
            is Equipment.HealOrBrace -> {
                isAttack = false
                if (s.playerHp >= s.playerMaxHp) {
                    s = s.copy(brace = s.brace + e.braceIfFull)
                    events += CombatEvent(
                        "${card.title}: Brace ${e.braceIfFull}",
                        FloatingText("BRACE", true), anim, sound,
                        glossaryHints = listOf("brace")
                    )
                } else {
                    val nh = (s.playerHp + e.heal).coerceAtMost(s.playerMaxHp)
                    s = s.copy(playerHp = nh)
                    events += CombatEvent(
                        "${card.title}: +${e.heal} HP",
                        FloatingText("+${e.heal}", true), anim, sound
                    )
                }
            }
        }
        return Triple(s, events, isAttack)
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
