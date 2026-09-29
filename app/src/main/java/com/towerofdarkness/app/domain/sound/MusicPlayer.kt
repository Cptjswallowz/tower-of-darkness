package com.towerofdarkness.app.domain.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.math.max
import kotlin.math.min

/**
 * v0.1.63-scorefade — app-wide bed player with dual-player loop crossfade.
 *
 * **Loop beds** (hub/path/combat/elite/boss): never sets [MediaPlayer.isLooping]
 * (that seekTo(0) click is the FAIL). Dual MediaPlayer: ~[LOOP_CROSSFADE_MS] before
 * end, start a second instance at volume 0 and crossfade, then release the first.
 *
 * **Once beds** (title/loadout/shop/rest + victory/defeat stings): play from start;
 * last ~[ONCE_TAIL_FADE_MS] linear fade to 0; then silence forever on that screen
 * ([onceFinishedBed]). Same-bed re-apply no-ops.
 *
 * Screen-change leave: ~[CROSSFADE_MS] fade-out then next bed (never hard cut).
 * Gain ~0.40; duck 0.18 / ~320ms; lifecycle pause/resume; ClimbIntro silence.
 */
class MusicPlayer(context: Context) {
    private val app = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())

    /** Active (audible / primary) player. */
    private var player: MediaPlayer? = null
    /** Second instance during dual-player loop handoff. */
    private var handoffPlayer: MediaPlayer? = null
    private var currentBed: MusicBed? = null
    private var currentIsSting: Boolean = false
    private var stingPlayed: MusicBed? = null
    /** Once bed that finished its tail fade while still on that screen. */
    private var onceFinishedBed: MusicBed? = null

    private var targetGain: Float = GAIN
    private var appliedGain: Float = 0f
    private var ducked: Boolean = false
    private var lifecyclePaused: Boolean = false
    private var silenced: Boolean = false
    private var loopHandoffInProgress: Boolean = false

    private var fadeRunnable: Runnable? = null
    private var duckRestore: Runnable? = null
    private var positionWatch: Runnable? = null

    fun apply(intent: MusicIntent) {
        when (intent) {
            is MusicIntent.Silence -> {
                silenced = true
                onceFinishedBed = null
                stopWatch()
                stopFade()
                fadeToThen(0f, CROSSFADE_MS) { releaseAll("silence") }
                currentBed = null
                currentIsSting = false
                Log.d(TAG, "MUSIC silence")
            }
            is MusicIntent.Loop -> {
                silenced = false
                stingPlayed = null
                // Same bed still playing, or once-finished silence on this screen — no restart
                if (currentBed == intent.bed && !currentIsSting) {
                    if (onceFinishedBed == intent.bed) {
                        Log.d(TAG, "MUSIC onceFinished no-op bed=${intent.bed.slot}")
                        return
                    }
                    if (player != null) {
                        ensurePlaying()
                        return
                    }
                }
                onceFinishedBed = null
                crossfadeTo(intent.bed, sting = false)
            }
            is MusicIntent.Sting -> {
                silenced = false
                onceFinishedBed = null
                if (stingPlayed == intent.bed && currentIsSting) return
                if (stingPlayed == intent.bed && currentBed == intent.bed) return
                stingPlayed = intent.bed
                crossfadeTo(intent.bed, sting = true)
            }
        }
    }

    fun duck() {
        if (player == null || silenced || lifecyclePaused) return
        stopDuckRestore()
        ducked = true
        applyVolumeNow(DUCK_GAIN)
        Log.d(TAG, "MUSIC duck")
        duckRestore = Runnable {
            ducked = false
            applyVolumeNow(targetGain)
            duckRestore = null
        }
        handler.postDelayed(duckRestore!!, DUCK_MS)
    }

    fun onAppBackground() {
        lifecyclePaused = true
        try {
            player?.takeIf { it.isPlaying }?.pause()
        } catch (_: Exception) {
        }
        try {
            handoffPlayer?.takeIf { it.isPlaying }?.pause()
        } catch (_: Exception) {
        }
        Log.d(TAG, "MUSIC pause lifecycle")
    }

    fun onAppForeground() {
        lifecyclePaused = false
        if (silenced) return
        ensurePlaying()
        try {
            handoffPlayer?.takeIf { !it.isPlaying }?.start()
        } catch (_: Exception) {
        }
        Log.d(TAG, "MUSIC resume lifecycle bed=${currentBed?.slot}")
    }

    fun release() {
        stopWatch()
        stopFade()
        stopDuckRestore()
        releaseAll("release")
        handler.removeCallbacksAndMessages(null)
    }

    private fun crossfadeTo(bed: MusicBed, sting: Boolean) {
        stopWatch()
        stopFade()
        loopHandoffInProgress = false
        val label = if (sting) "sting" else if (bed.loops) "loop" else "once"
        Log.d(TAG, "MUSIC $label=${bed.slot} gain=$GAIN")
        if (player == null && handoffPlayer == null) {
            startPlayer(bed, sting, fadeIn = true)
            return
        }
        // Leave mid-track: 400ms fade out → next bed (never hard cut)
        fadeToThen(0f, CROSSFADE_MS) {
            releaseAll("crossfade")
            if (!silenced) startPlayer(bed, sting, fadeIn = true)
        }
    }

    private fun startPlayer(bed: MusicBed, sting: Boolean, fadeIn: Boolean) {
        try {
            val mp = createPreparedPlayer(bed)
            appliedGain = if (fadeIn) 0f else GAIN
            targetGain = GAIN
            ducked = false
            mp.setVolume(appliedGain, appliedGain)
            player = mp
            currentBed = bed
            currentIsSting = sting
            if (!lifecyclePaused && !silenced) {
                mp.start()
            }
            if (fadeIn) {
                fadeToThen(GAIN, CROSSFADE_MS) {
                    armPlaybackWatch(bed, sting, mp)
                }
            } else {
                armPlaybackWatch(bed, sting, mp)
            }
        } catch (e: Exception) {
            Log.w(TAG, "MUSIC start failed bed=${bed.slot}: ${e.message}")
            releaseAll("start_fail")
        }
    }

    /**
     * Create a prepared, non-looping MediaPlayer for [bed].
     * **Never** sets MediaPlayer looping — that seek(0) click is the FAIL for loop beds.
     */
    private fun createPreparedPlayer(bed: MusicBed): MediaPlayer {
        val mp = MediaPlayer()
        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        app.assets.openFd(bed.assetPath).use { fd ->
            mp.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
        }
        // Explicit: loop beds use dual-player crossfade, not MediaPlayer looping.
        mp.isLooping = false
        mp.setOnErrorListener { _, what, extra ->
            Log.w(TAG, "MUSIC error what=$what extra=$extra bed=${bed.slot}")
            releaseAll("error")
            true
        }
        mp.prepare()
        return mp
    }

    private fun armPlaybackWatch(bed: MusicBed, sting: Boolean, mp: MediaPlayer) {
        stopWatch()
        if (sting || !bed.loops) {
            // Once / sting: tail fade in last ONCE_TAIL_FADE_MS, then silence
            scheduleOnceTailWatch(bed, sting, mp)
        } else {
            // Loop: dual-player crossfade near end
            scheduleLoopWatch(bed, mp)
        }
    }

    private fun scheduleOnceTailWatch(bed: MusicBed, sting: Boolean, mp: MediaPlayer) {
        val watch = object : Runnable {
            override fun run() {
                if (player !== mp || silenced) return
                try {
                    val dur = mp.duration
                    val pos = mp.currentPosition
                    if (dur <= 0) {
                        handler.postDelayed(this, WATCH_INTERVAL_MS)
                        return
                    }
                    val remaining = dur - pos
                    if (remaining <= ONCE_TAIL_FADE_MS) {
                        positionWatch = null
                        val fadeMs = max(200L, remaining.toLong().coerceAtMost(ONCE_TAIL_FADE_MS))
                        Log.d(TAG, "MUSIC onceTail bed=${bed.slot} fadeMs=$fadeMs")
                        fadeToThen(0f, fadeMs) {
                            releaseAll("once_done")
                            currentBed = bed
                            currentIsSting = false
                            if (sting) {
                                // Sting finished — stay silent until next nav bed
                            } else {
                                onceFinishedBed = bed
                            }
                        }
                        return
                    }
                    handler.postDelayed(this, WATCH_INTERVAL_MS)
                } catch (_: Exception) {
                    positionWatch = null
                }
            }
        }
        positionWatch = watch
        handler.postDelayed(watch, WATCH_INTERVAL_MS)
        // Backup: if completion fires before watch (very short file), release cleanly
        mp.setOnCompletionListener {
            if (player !== mp) return@setOnCompletionListener
            stopWatch()
            stopFade()
            releaseAll("once_done")
            currentBed = bed
            currentIsSting = false
            if (!sting) onceFinishedBed = bed
        }
    }

    private fun scheduleLoopWatch(bed: MusicBed, mp: MediaPlayer) {
        val watch = object : Runnable {
            override fun run() {
                if (player !== mp || silenced || loopHandoffInProgress) return
                try {
                    val dur = mp.duration
                    val pos = mp.currentPosition
                    if (dur <= 0) {
                        handler.postDelayed(this, WATCH_INTERVAL_MS)
                        return
                    }
                    val remaining = dur - pos
                    if (remaining <= LOOP_CROSSFADE_MS && remaining > 0) {
                        positionWatch = null
                        startDualLoopHandoff(bed, mp, remaining.toLong())
                        return
                    }
                    // Past end without handoff (edge): fallback restart
                    if (remaining <= 0 || !mp.isPlaying) {
                        positionWatch = null
                        fallbackLoopRestart(bed)
                        return
                    }
                    handler.postDelayed(this, WATCH_INTERVAL_MS)
                } catch (_: Exception) {
                    positionWatch = null
                }
            }
        }
        positionWatch = watch
        handler.postDelayed(watch, WATCH_INTERVAL_MS)
        mp.setOnCompletionListener {
            // Should not normally fire if dual handoff works; fallback if it does
            if (player !== mp || loopHandoffInProgress) return@setOnCompletionListener
            stopWatch()
            fallbackLoopRestart(bed)
        }
    }

    /**
     * Dual-player seamless loop: start second instance at 0, crossfade over [windowMs],
     * release the ending player. Primary shipped path for LOOP beds.
     */
    private fun startDualLoopHandoff(bed: MusicBed, ending: MediaPlayer, windowMs: Long) {
        if (loopHandoffInProgress) return
        loopHandoffInProgress = true
        val fadeMs = windowMs.coerceIn(200L, LOOP_CROSSFADE_MS)
        Log.d(TAG, "MUSIC dualCrossfade bed=${bed.slot} fadeMs=$fadeMs")
        try {
            val next = createPreparedPlayer(bed)
            next.setVolume(0f, 0f)
            handoffPlayer = next
            if (!lifecyclePaused && !silenced) {
                next.start()
            }
            dualCrossfade(ending, next, fadeMs) {
                try {
                    ending.setOnCompletionListener(null)
                    ending.setOnErrorListener(null)
                    ending.stop()
                } catch (_: Exception) {
                }
                try {
                    ending.release()
                } catch (_: Exception) {
                }
                if (player === ending) {
                    player = next
                }
                handoffPlayer = null
                loopHandoffInProgress = false
                appliedGain = if (ducked) DUCK_GAIN else GAIN
                targetGain = GAIN
                if (!silenced && currentBed == bed) {
                    scheduleLoopWatch(bed, next)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "MUSIC dualCrossfade failed, fallback: ${e.message}")
            loopHandoffInProgress = false
            handoffPlayer = null
            fallbackLoopRestart(bed)
        }
    }

    /** Fallback only if dual impossible: fade out 1.2s then fade in same file from 0. */
    private fun fallbackLoopRestart(bed: MusicBed) {
        Log.d(TAG, "MUSIC fallbackFadeRestart bed=${bed.slot}")
        stopWatch()
        fadeToThen(0f, FALLBACK_FADE_MS) {
            releaseAll("fallback")
            if (!silenced && (currentBed == bed || currentBed == null)) {
                currentBed = bed
                startPlayer(bed, sting = false, fadeIn = true)
            }
        }
    }

    private fun dualCrossfade(
        ending: MediaPlayer,
        next: MediaPlayer,
        durationMs: Long,
        then: () -> Unit
    ) {
        stopFade()
        val steps = max(1, (durationMs / FADE_STEP_MS).toInt())
        var i = 0
        val startOut = try {
            // Prefer current appliedGain when not ducked
            if (ducked) GAIN else appliedGain.coerceAtLeast(0.01f)
        } catch (_: Exception) {
            GAIN
        }
        val runnable = object : Runnable {
            override fun run() {
                i++
                val t = min(1f, i.toFloat() / steps)
                val outV = startOut * (1f - t)
                val inV = GAIN * t
                val duckMul = if (ducked) DUCK_GAIN / GAIN else 1f
                try {
                    ending.setVolume(outV * duckMul, outV * duckMul)
                } catch (_: Exception) {
                }
                try {
                    next.setVolume(inV * duckMul, inV * duckMul)
                } catch (_: Exception) {
                }
                appliedGain = inV
                if (i >= steps) {
                    fadeRunnable = null
                    targetGain = GAIN
                    then()
                } else {
                    handler.postDelayed(this, FADE_STEP_MS)
                }
            }
        }
        fadeRunnable = runnable
        handler.post(runnable)
    }

    private fun ensurePlaying() {
        val mp = player ?: return
        try {
            if (!lifecyclePaused && !silenced && !mp.isPlaying) {
                mp.start()
            }
            applyVolumeNow(if (ducked) DUCK_GAIN else targetGain)
        } catch (_: Exception) {
        }
    }

    private fun fadeToThen(end: Float, durationMs: Long, then: () -> Unit) {
        stopFade()
        val mp = player
        if (mp == null) {
            appliedGain = end
            then()
            return
        }
        val start = appliedGain
        val steps = max(1, (durationMs / FADE_STEP_MS).toInt())
        var i = 0
        val runnable = object : Runnable {
            override fun run() {
                i++
                val t = min(1f, i.toFloat() / steps)
                val v = start + (end - start) * t
                appliedGain = v
                if (!ducked) {
                    try {
                        mp.setVolume(v, v)
                    } catch (_: Exception) {
                    }
                    // Also fade handoff if present (screen leave mid-handoff)
                    try {
                        handoffPlayer?.setVolume(v, v)
                    } catch (_: Exception) {
                    }
                }
                if (i >= steps) {
                    fadeRunnable = null
                    targetGain = end
                    then()
                } else {
                    handler.postDelayed(this, FADE_STEP_MS)
                }
            }
        }
        fadeRunnable = runnable
        handler.post(runnable)
    }

    private fun applyVolumeNow(v: Float) {
        appliedGain = v
        if (!ducked) targetGain = max(targetGain, if (v >= DUCK_GAIN) GAIN else targetGain)
        try {
            player?.setVolume(v, v)
        } catch (_: Exception) {
        }
        // During dual handoff, keep next at proportional level if ducked mid-fade
        try {
            handoffPlayer?.let { hp ->
                if (ducked) hp.setVolume(v, v)
            }
        } catch (_: Exception) {
        }
    }

    private fun releaseAll(reason: String) {
        stopWatch()
        loopHandoffInProgress = false
        releaseOne(handoffPlayer)
        handoffPlayer = null
        releaseOne(player)
        player = null
        if (reason == "silence" || reason == "release" || reason == "error" || reason == "start_fail") {
            currentBed = null
            currentIsSting = false
            onceFinishedBed = null
        }
        if (reason == "crossfade" || reason == "fallback") {
            // bed replaced by caller
            currentIsSting = false
        }
        // once_done: caller sets currentBed / onceFinishedBed
        appliedGain = 0f
    }

    private fun releaseOne(mp: MediaPlayer?) {
        if (mp == null) return
        try {
            mp.setOnCompletionListener(null)
            mp.setOnErrorListener(null)
            mp.stop()
        } catch (_: Exception) {
        }
        try {
            mp.release()
        } catch (_: Exception) {
        }
    }

    private fun stopWatch() {
        positionWatch?.let { handler.removeCallbacks(it) }
        positionWatch = null
    }

    private fun stopFade() {
        fadeRunnable?.let { handler.removeCallbacks(it) }
        fadeRunnable = null
    }

    private fun stopDuckRestore() {
        duckRestore?.let { handler.removeCallbacks(it) }
        duckRestore = null
    }

    companion object {
        private const val TAG = "MUSIC"
        const val GAIN = 0.40f
        const val DUCK_GAIN = 0.18f
        /** Screen-change / leave mid-track fade. */
        const val CROSSFADE_MS = 400L
        const val DUCK_MS = 320L
        /** Dual-player loop handoff window (~1.5–2.0s before end). */
        const val LOOP_CROSSFADE_MS = 1800L
        /** Once-bed linear tail fade (~2.0s in 1.8–2.5s range). */
        const val ONCE_TAIL_FADE_MS = 2000L
        /** Fallback fade-out before restart when dual handoff fails. */
        const val FALLBACK_FADE_MS = 1200L
        private const val FADE_STEP_MS = 40L
        private const val WATCH_INTERVAL_MS = 200L
    }
}
