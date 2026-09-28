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
 * v0.1.62-score — single app-wide bed player (one bed at a time).
 *
 * Gain ~0.40; screen change 400ms fade-out then fade-in; same bed no-ops.
 * Victory/defeat are one-shot stingers. Lifecycle pause/resume. Duck on combat SFX.
 */
class MusicPlayer(context: Context) {
    private val app = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())

    private var player: MediaPlayer? = null
    private var currentBed: MusicBed? = null
    private var currentIsSting: Boolean = false
    private var stingPlayed: MusicBed? = null

    private var targetGain: Float = GAIN
    private var appliedGain: Float = 0f
    private var ducked: Boolean = false
    private var lifecyclePaused: Boolean = false
    private var silenced: Boolean = false

    private var fadeRunnable: Runnable? = null
    private var duckRestore: Runnable? = null

    fun apply(intent: MusicIntent) {
        when (intent) {
            is MusicIntent.Silence -> {
                silenced = true
                stopFade()
                fadeToThen(0f) { releasePlayer("silence") }
                currentBed = null
                currentIsSting = false
                Log.d(TAG, "MUSIC silence")
            }
            is MusicIntent.Loop -> {
                silenced = false
                stingPlayed = null
                if (currentBed == intent.bed && !currentIsSting && player != null) {
                    // Same bed re-entry — no restart
                    ensurePlaying()
                    return
                }
                crossfadeTo(intent.bed, loop = true, sting = false)
            }
            is MusicIntent.Sting -> {
                silenced = false
                if (stingPlayed == intent.bed && currentIsSting) return
                if (stingPlayed == intent.bed && currentBed == intent.bed) return
                stingPlayed = intent.bed
                crossfadeTo(intent.bed, loop = false, sting = true)
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
        Log.d(TAG, "MUSIC pause lifecycle")
    }

    fun onAppForeground() {
        lifecyclePaused = false
        if (silenced) return
        ensurePlaying()
        Log.d(TAG, "MUSIC resume lifecycle bed=${currentBed?.slot}")
    }

    fun release() {
        stopFade()
        stopDuckRestore()
        releasePlayer("release")
        handler.removeCallbacksAndMessages(null)
    }

    private fun crossfadeTo(bed: MusicBed, loop: Boolean, sting: Boolean) {
        stopFade()
        val label = if (sting) "sting" else "bed"
        Log.d(TAG, "MUSIC $label=${bed.slot} gain=$GAIN")
        if (player == null) {
            startPlayer(bed, loop, sting, fadeIn = true)
            return
        }
        // Fade out current, then swap + fade in
        fadeToThen(0f) {
            releasePlayer("crossfade")
            if (!silenced) startPlayer(bed, loop, sting, fadeIn = true)
        }
    }

    private fun startPlayer(bed: MusicBed, loop: Boolean, sting: Boolean, fadeIn: Boolean) {
        try {
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
            mp.isLooping = loop
            mp.setOnCompletionListener {
                if (sting) {
                    // Sting finished — stay silent until next nav bed
                    releasePlayer("sting_done")
                    currentIsSting = false
                }
            }
            mp.setOnErrorListener { _, what, extra ->
                Log.w(TAG, "MUSIC error what=$what extra=$extra bed=${bed.slot}")
                releasePlayer("error")
                true
            }
            mp.prepare()
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
            if (fadeIn) fadeToThen(GAIN) {}
        } catch (e: Exception) {
            Log.w(TAG, "MUSIC start failed bed=${bed.slot}: ${e.message}")
            releasePlayer("start_fail")
        }
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

    private fun fadeToThen(end: Float, then: () -> Unit) {
        stopFade()
        val mp = player
        if (mp == null) {
            appliedGain = end
            then()
            return
        }
        val start = appliedGain
        val steps = max(1, (CROSSFADE_MS / FADE_STEP_MS).toInt())
        var i = 0
        val step = Runnable {
            // re-posted below
        }
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
    }

    private fun releasePlayer(reason: String) {
        try {
            player?.setOnCompletionListener(null)
            player?.setOnErrorListener(null)
            player?.stop()
        } catch (_: Exception) {
        }
        try {
            player?.release()
        } catch (_: Exception) {
        }
        player = null
        if (reason != "crossfade" && reason != "sting_done") {
            // keep currentBed for sting_done silence; clear otherwise when full stop
        }
        if (reason == "silence" || reason == "release" || reason == "error" || reason == "start_fail") {
            currentBed = null
            currentIsSting = false
        }
        if (reason == "sting_done") {
            currentBed = null
        }
        appliedGain = 0f
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
        const val CROSSFADE_MS = 400L
        const val DUCK_MS = 320L
        private const val FADE_STEP_MS = 40L
    }
}
