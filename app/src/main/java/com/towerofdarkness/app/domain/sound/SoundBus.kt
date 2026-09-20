package com.towerofdarkness.app.domain.sound

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.ToneGenerator
import android.media.AudioManager

interface SoundBus {
    fun play(event: String)
    fun release()
}

class AssetSoundBus(context: Context) : SoundBus {
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()
    private val ids = mutableMapOf<String, Int>()
    private val tone: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 40)
    } catch (_: Exception) { null }

    init {
        val map = mapOf(
            "dice" to "audio/sfx/sfx_dice.wav",
            "card_fire" to "audio/sfx/sfx_card_fire.wav",
            "hit" to "audio/sfx/sfx_hit.wav",
            "legendary" to "audio/sfx/sfx_legendary_sting.wav",
            "miss" to "audio/sfx/sfx_miss.wav"
        )
        map.forEach { (key, path) ->
            try {
                context.assets.openFd(path).use { fd: AssetFileDescriptor ->
                    ids[key] = pool.load(fd, 1)
                }
            } catch (_: Exception) { /* PH missing — tone fallback */ }
        }
    }

    override fun play(event: String) {
        val id = ids[event]
        if (id != null) {
            pool.play(id, 0.8f, 0.8f, 1, 0, 1f)
            return
        }
        try {
            val t = when (event) {
                "dice" -> ToneGenerator.TONE_PROP_BEEP
                "legendary" -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
                "miss" -> ToneGenerator.TONE_PROP_NACK
                else -> ToneGenerator.TONE_DTMF_1
            }
            tone?.startTone(t, 100)
        } catch (_: Exception) {}
    }

    override fun release() {
        pool.release()
        try { tone?.release() } catch (_: Exception) {}
    }
}

object SilentSoundBus : SoundBus {
    override fun play(event: String) {}
    override fun release() {}
}
