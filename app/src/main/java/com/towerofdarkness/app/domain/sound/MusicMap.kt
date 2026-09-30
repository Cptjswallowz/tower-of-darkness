package com.towerofdarkness.app.domain.sound

import com.towerofdarkness.app.domain.combat.CombatState
import com.towerofdarkness.app.domain.combat.Enemy
import com.towerofdarkness.app.domain.combat.EnemyKind
import com.towerofdarkness.app.domain.combat.HallwayPacks
import com.towerofdarkness.app.nav.NavState
import com.towerofdarkness.app.nav.RunSummaryData

/**
 * v0.1.62-score — NavState / combat enemy → music intent.
 *
 * Critical CoS: while Combat is in VICTORY phase waiting for Continue,
 * KEEP combat/boss/elite bed — do NOT start victory.ogg until Continue
 * advances to Run Summary (win). Defeat sting on summary lose entry.
 */
sealed class MusicIntent {
    data class Loop(val bed: MusicBed) : MusicIntent()
    data class Sting(val bed: MusicBed) : MusicIntent()
    /** Pause/mute during climb_intro.mp4 — do not fight video audio. */
    data object Silence : MusicIntent()
}

object MusicMap {
    fun resolve(
        nav: NavState,
        combat: CombatState?,
        summary: RunSummaryData?
    ): MusicIntent = when (nav) {
        NavState.MainMenu, NavState.Settings -> MusicIntent.Loop(MusicBed.TITLE)
        NavState.MetaHub -> MusicIntent.Loop(MusicBed.HUB)
        NavState.ClimbIntro -> MusicIntent.Silence
        NavState.Path, NavState.FloorBreak, NavState.Tutorial,
        NavState.Event, NavState.Treasure -> MusicIntent.Loop(MusicBed.PATH)
        NavState.Loadout -> MusicIntent.Loop(MusicBed.LOADOUT)
        NavState.Shop -> MusicIntent.Loop(MusicBed.SHOP)
        NavState.Rest -> MusicIntent.Loop(MusicBed.REST)
        NavState.Combat -> MusicIntent.Loop(combatBed(combat?.enemy))
        NavState.RunSummary -> {
            if (summary?.won == true) MusicIntent.Sting(MusicBed.VICTORY)
            else MusicIntent.Sting(MusicBed.DEFEAT)
        }
    }

    /** Trash → combat; Cave Troll / elite → elite; floor boss / Gate / Seal-Warden → boss. */
    fun combatBed(enemy: Enemy?): MusicBed {
        if (enemy == null) return MusicBed.COMBAT
        if (enemy.isBoss || HallwayPacks.isBossKind(enemy.kind)) return MusicBed.BOSS
        if (enemy.kind == EnemyKind.CAVE_TROLL) return MusicBed.ELITE
        return MusicBed.COMBAT
    }

    /** Keys that duck the bed briefly (SFX gain untouched). */
    val DUCK_KEYS: Set<String> = setOf(
        IronclashSfx.KEY_SWING,
        IronclashSfx.KEY_IMPACT,
        IronclashSfx.KEY_WAKE_SWING,
        IronclashSfx.KEY_WAKE_IMPACT,
        IronclashSfx.KEY_BRACE,
        IronclashSfx.KEY_EMBER,
        IronclashSfx.KEY_DICE,
        "slash_impact"
    )
}
