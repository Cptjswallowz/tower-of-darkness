package com.towerofdarkness.app.domain.sound

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import android.util.Log

interface SoundBus {
    fun play(event: String)
    /** Play all sounds from one combat beat; applies same-frame Impact-wins / garnish-duck. */
    fun playFrame(sounds: List<String>)
    fun release()
}

class AssetSoundBus(context: Context) : SoundBus {
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()
    private val ids = mutableMapOf<String, Int>()
    private val tone: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 40)
    } catch (_: Exception) {
        null
    }

    init {
        IronclashSfx.LOAD_MAP.forEach { (key, path) ->
            try {
                context.assets.openFd(path).use { fd: AssetFileDescriptor ->
                    ids[key] = pool.load(fd, 1)
                }
            } catch (_: Exception) {
                /* missing — tone fallback */
            }
        }
    }

    override fun play(event: String) {
        if (event.isBlank()) return
        playFrame(listOf(event))
    }

    override fun playFrame(sounds: List<String>) {
        val resolved = IronclashSfx.resolveFrame(sounds)
        for ((key, vol) in resolved) {
            playKey(key, vol)
        }
    }

    private fun playKey(key: String, volume: Float) {
        val id = ids[key]
        if (id != null) {
            pool.play(id, volume, volume, 1, 0, 1f)
            logPlay(key)
            return
        }
        try {
            val t = when (key) {
                IronclashSfx.KEY_DICE -> ToneGenerator.TONE_PROP_BEEP
                IronclashSfx.KEY_LEGENDARY, IronclashSfx.KEY_WAKE_CLASH ->
                    ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
                IronclashSfx.KEY_MISS -> ToneGenerator.TONE_PROP_NACK
                IronclashSfx.KEY_UI -> ToneGenerator.TONE_PROP_ACK
                else -> ToneGenerator.TONE_DTMF_1
            }
            tone?.startTone(t, 100)
            logPlay(key)
        } catch (_: Exception) {
        }
    }

    private fun logPlay(key: String) {
        val clip = when (key) {
            IronclashSfx.KEY_IMPACT -> IronclashSfx.FILE_IMPACT
            IronclashSfx.KEY_WAKE_CLASH -> IronclashSfx.FILE_WAKE_CLASH
            IronclashSfx.KEY_BRACE -> IronclashSfx.FILE_BRACE
            IronclashSfx.KEY_EMBER -> IronclashSfx.FILE_EMBER
            IronclashSfx.KEY_SOFTEN -> IronclashSfx.FILE_SOFTEN
            IronclashSfx.KEY_UI -> IronclashSfx.FILE_UI
            IronclashSfx.KEY_LEGENDARY -> IronclashSfx.FILE_STING
            IronclashSfx.KEY_DICE -> IronclashSfx.FILE_DICE
            IronclashSfx.KEY_MISS -> IronclashSfx.FILE_MISS
            else -> key
        }
        val role = when (key) {
            IronclashSfx.KEY_WAKE_CLASH -> "wake"
            IronclashSfx.KEY_LEGENDARY -> "wake"
            else -> key
        }
        Log.d("SFX", "$role $clip")
    }

    override fun release() {
        pool.release()
        try {
            tone?.release()
        } catch (_: Exception) {
        }
    }
}

object SilentSoundBus : SoundBus {
    override fun play(event: String) {}
    override fun playFrame(sounds: List<String>) {}
    override fun release() {}
}
