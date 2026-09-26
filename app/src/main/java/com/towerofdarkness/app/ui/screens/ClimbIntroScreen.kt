package com.towerofdarkness.app.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.towerofdarkness.app.nav.ClimbIntroGate
import com.towerofdarkness.app.nav.GameController
import com.towerofdarkness.app.ui.theme.Bone
import com.towerofdarkness.app.ui.theme.Gold

/**
 * Full-screen Title→Climb intro. Dark behind; FIT (letterbox, no oval crop).
 * Tap anywhere or Skip → Floor 1 path via [GameController.finishClimbIntro].
 */
@Composable
fun ClimbIntroScreen(gc: GameController) {
    val context = LocalContext.current
    var finished by remember { mutableStateOf(false) }

    fun finishOnce() {
        if (finished) return
        finished = true
        gc.finishClimbIntro()
    }

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = true
            setMediaItem(MediaItem.fromUri("asset:///${ClimbIntroGate.ASSET_NAME}"))
            prepare()
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) finishOnce()
            }

            override fun onPlayerError(error: PlaybackException) {
                finishOnce()
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    this.player = player
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)
                    isClickable = false
                    isFocusable = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        // Transparent overlay so taps reach Compose (PlayerView would eat them).
        Box(
            Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { finishOnce() }
        )
        Text(
            text = "Skip",
            color = Gold,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { finishOnce() }
        )
        Text(
            text = "Tap to skip",
            color = Bone.copy(alpha = 0.45f),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        )
    }
}
