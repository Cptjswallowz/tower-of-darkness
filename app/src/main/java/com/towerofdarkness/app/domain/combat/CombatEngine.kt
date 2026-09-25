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
    val goldLog: Boolean = false,
    /** Presentation only (v0.1.14): Brace absorbed on this hit; 0 = none. */
    val braceAbsorbed: Int = 0
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
    /** Soften remaining — reduces next damaging enemy kit skill (not Hide family / Nip). */
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
    val pinnedWakeLine: String? = null,
    /** Enemy kit spent tile ids (same exhaust machine as player). */
    val enemySpentIds: Set<String> = emptySet(),
    val enemyHighlightedId: String? = null,
    /** Brace on the enemy (Hide / Rust Guard / Cinder Hide). Persists until eaten. */
    val enemyBrace: Int = 0
)

class CombatEngine(private val rng: Random = Random.Default) {

    fun start(
        activeCards: List<Card>,
        enemy: Enemy,
        weapon: WeaponRuntime,
        maxHp: Int = Balance.PLAYER_MAX_HP,
        playerHp: Int = maxHp,
        initialBrace: Int = 0
    ): CombatState = CombatState(
        playerHp = playerHp.coerceIn(1, maxHp),
        playerMaxHp = maxHp,
        brace = initialBrace.coerceAtLeast(0),
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
        val card = pickWeighted(live) { it.weight } ?: return state.copy(log = state.log + events)
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
            val applied = applyDamageToEnemy(s, dmg)
            s = applied.state
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
            if (s.enemy.hp <= 0) fullKill = true
            w = w.copy(charge = 0)
            s = s.copy(weapon = w, fullProcThisCombat = fullProc, fullProcKilled = fullKill)
        }

        if (doSpark) {
            val dmg = w.def.sparkDmg(w.level)
            val applied = applyDamageToEnemy(s, dmg)
            s = applied.state
            // Spark: plain log only — no anim, no gold, no WAKE float
            events += CombatEvent(
                message = "Ashbrand spark ($dmg)",
                floating = null,
                animStyle = CombatAnimStyle.QUICK,
                sound = "card_fire",
                goldLog = false
            )
            s = s.copy(weapon = w)
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

    /**
     * F — enemy live kit (v0.1.32): same weight / grey among unspent tiles.
     * Soften → next damaging skill (not Hide family); Nip ignores Soften.
     * Brace on You still absorbs before HP.
     */
    fun resolveEnemy(state: CombatState): CombatState {
        if (state.enemy.hp <= 0) return finishVictory(state)
        val kit = EnemyKits.skillsFor(state.enemy)
        var spent = state.enemySpentIds
        val events = mutableListOf<CombatEvent>()
        if (spent.size >= kit.size && kit.isNotEmpty()) {
            spent = emptySet()
            events += CombatEvent("Enemy cycle reset", sound = "ui")
        }
        val live = kit.filter { it.id !in spent }
        val skill = pickWeighted(live) { it.weight }
            ?: return state.copy(log = state.log + events, beat = CombatBeat.AFTER_ENEMY)
        events += CombatEvent("${state.enemy.kind.displayName} winds up…", sound = "ui")

        var s = state.copy(
            enemySpentIds = spent + skill.id,
            enemyHighlightedId = skill.id
        )
        var soften = s.counterPenalty

        when (skill.kind) {
            EnemySkillKind.BRACE -> {
                val gain = skill.braceGain
                s = s.copy(enemyBrace = s.enemyBrace + gain)
                val msg = "${s.enemy.kind.displayName} — ${skill.title} $gain"
                events += CombatEvent(
                    msg,
                    FloatingText("BRACE $gain", false),
                    sound = "ui",
                    glossaryHints = listOf(skill.glossaryKey, "brace")
                )
                // Soften pip stays — Hide family does not consume Soften
            }
            EnemySkillKind.NIP -> {
                val raw = skill.rollDamage(rng)
                // Nip ignores Soften; Soften does not clear
                val (afterHit, hpDmg, absorbed) = hitPlayer(s, raw)
                s = afterHit
                val msg = buildString {
                    append("${s.enemy.kind.displayName} — ${skill.title} $raw (ignores Soften)")
                    if (absorbed > 0) append(" ($absorbed Brace)")
                }
                events += CombatEvent(
                    msg,
                    FloatingText("-$hpDmg", false),
                    sound = "hit",
                    glossaryHints = listOf(skill.glossaryKey, "nip", "soften") +
                        if (absorbed > 0) listOf("brace") else emptyList(),
                    braceAbsorbed = absorbed
                )
            }
            EnemySkillKind.DAMAGE -> {
                var raw = skill.rollDamage(rng)
                if (soften > 0) {
                    raw = (raw - soften).coerceAtLeast(1)
                    soften = 0
                }
                val (afterHit, hpDmg, absorbed) = hitPlayer(s, raw)
                s = afterHit.copy(counterPenalty = soften)
                val msg = buildString {
                    append("${s.enemy.kind.displayName} — ${skill.title} $raw")
                    if (absorbed > 0) append(" ($absorbed Brace)")
                }
                events += CombatEvent(
                    msg,
                    FloatingText("-$hpDmg", false),
                    sound = "hit",
                    glossaryHints = listOf(skill.glossaryKey) +
                        if (absorbed > 0) listOf("brace") else emptyList(),
                    braceAbsorbed = absorbed
                )
            }
        }

        if (s.playerHp <= 0) {
            events += CombatEvent("Defeat…", FloatingText("DOWN", false), sound = "miss")
            return s.copy(
                playerHp = 0,
                brace = 0,
                counterPenalty = 0,
                log = state.log + events,
                finished = true,
                playerWon = false,
                highlightedId = null,
                enemyHighlightedId = skill.id,
                enemySpentIds = spent + skill.id,
                beat = CombatBeat.AWAITING_CONTINUE,
                round = state.round + 1
            )
        }
        return s.copy(
            log = state.log + events,
            highlightedId = null,
            beat = CombatBeat.AFTER_ENEMY,
            round = state.round + 1
        )
    }

    /** Advance to next round — clear unused Brace leftover per cards-v0. Enemy Brace persists. */
    fun readyNext(state: CombatState): CombatState =
        state.copy(beat = CombatBeat.READY, weaponFlashed = false, brace = 0, enemyHighlightedId = null)

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

    /** Player Brace absorbs before HP. Returns (state, hpDamage, absorbed). */
    private fun hitPlayer(state: CombatState, rawDmg: Int): Triple<CombatState, Int, Int> {
        var dmg = rawDmg.coerceAtLeast(0)
        var brace = state.brace
        var absorbed = 0
        if (brace > 0 && dmg > 0) {
            absorbed = minOf(brace, dmg)
            brace -= absorbed
            dmg -= absorbed
        }
        val newHp = (state.playerHp - dmg).coerceAtLeast(0)
        return Triple(state.copy(playerHp = newHp, brace = brace), dmg, absorbed)
    }

    /** Enemy Brace absorbs player damage before enemy HP. */
    private data class EnemyDmgResult(val state: CombatState, val hpDamage: Int, val absorbed: Int)

    private fun applyDamageToEnemy(state: CombatState, raw: Int): EnemyDmgResult {
        var dmg = raw.coerceAtLeast(0)
        var eBrace = state.enemyBrace
        var absorbed = 0
        if (eBrace > 0 && dmg > 0) {
            absorbed = minOf(eBrace, dmg)
            eBrace -= absorbed
            dmg -= absorbed
        }
        val enemy = state.enemy.copy(hp = (state.enemy.hp - dmg).coerceAtLeast(0))
        return EnemyDmgResult(state.copy(enemy = enemy, enemyBrace = eBrace), dmg, absorbed)
    }

    private fun resolveCard(
        state: CombatState, card: Card, anim: CombatAnimStyle
    ): Triple<CombatState, List<CombatEvent>, Boolean> {
        val events = mutableListOf<CombatEvent>()
        var s = state
        val sound = if (card.rarity == Rarity.RARE) "legendary" else "card_fire"
        var isAttack = false

        when (val e = card.effect) {
            is SkillEffect.Damage -> {
                isAttack = true
                val applied = applyDamageToEnemy(s, e.damage)
                s = applied.state
                events += CombatEvent(
                    "${card.title} deals ${e.damage}",
                    FloatingText("-${e.damage}", true, card.rarity == Rarity.RARE),
                    anim, sound
                )
            }
            is SkillEffect.DamageAndHeal -> {
                isAttack = true
                val applied = applyDamageToEnemy(s, e.damage)
                s = applied.state
                val nh = (s.playerHp + e.heal).coerceAtMost(s.playerMaxHp)
                events += CombatEvent(
                    "${card.title}: ${e.damage} dmg, +${e.heal} HP",
                    FloatingText("-${e.damage}", true), anim, sound
                )
                s = s.copy(playerHp = nh)
            }
            is SkillEffect.DamageAndBraceIfAshPips -> {
                isAttack = true
                val applied = applyDamageToEnemy(s, e.damage)
                s = applied.state
                events += CombatEvent(
                    "${card.title} deals ${e.damage}",
                    FloatingText("-${e.damage}", true), anim, sound
                )
                var brace = s.brace
                if (s.weapon.pipsFilled >= e.minPips) {
                    brace += e.brace
                    events += CombatEvent(
                        "Brace +${e.brace}",
                        FloatingText("BRACE", true), anim, sound,
                        glossaryHints = listOf("brace")
                    )
                }
                s = s.copy(brace = brace)
            }
            is MoveEffect.DamageAndSoften -> {
                isAttack = true
                val applied = applyDamageToEnemy(s, e.damage)
                s = applied.state
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
                s = s.copy(brace = brace, counterPenalty = s.counterPenalty + e.counterPenalty)
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

    private fun <T> pickWeighted(items: List<T>, weightOf: (T) -> Int): T? {
        if (items.isEmpty()) return null
        val total = items.sumOf { weightOf(it) }
        if (total <= 0) return items.last()
        var r = rng.nextInt(total)
        for (item in items) {
            r -= weightOf(item)
            if (r < 0) return item
        }
        return items.last()
    }
}
