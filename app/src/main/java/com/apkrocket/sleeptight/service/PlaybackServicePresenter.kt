package com.apkrocket.sleeptight.service

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.apkrocket.sleeptight.audio.SoundEngine

/**
 * Keeps the foreground [PlaybackService] started for exactly as long as a sound is selected.
 * Mounted once at the app root so it survives navigation between screens.
 */
class PlaybackServicePresenter(
    private val soundEngine: SoundEngine = SoundEngine,
) {

    @Composable
    fun present() {
        val context = LocalContext.current
        val hasSound = soundEngine.state.collectAsState().value.type != null

        LaunchedEffect(hasSound) {
            val intent = Intent(context, PlaybackService::class.java)
            if (hasSound) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.stopService(intent)
            }
        }
    }
}
