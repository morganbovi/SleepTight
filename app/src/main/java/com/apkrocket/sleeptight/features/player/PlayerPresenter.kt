package com.apkrocket.sleeptight.features.player

import android.content.Context
import android.media.AudioManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkrocket.sleeptight.audio.SoundEngine
import com.apkrocket.sleeptight.features.player.PlayerUiModel.Event.AboutClicked
import com.apkrocket.sleeptight.features.player.PlayerUiModel.Event.ChooseSoundClicked
import com.apkrocket.sleeptight.features.player.PlayerUiModel.Event.PlayPauseClicked
import com.apkrocket.sleeptight.features.player.PlayerUiModel.Event.VolumeDragged
import com.apkrocket.sleeptight.ui.navigation.LocalScreenNavigator
import com.apkrocket.sleeptight.ui.presenter.EventHandler
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** How often we poll the system volume for changes we didn't cause ourselves. */
private const val VOLUME_POLL_INTERVAL_MS = 150L

class PlayerPresenter(
    private val soundEngine: SoundEngine = SoundEngine,
) {

    @Composable
    fun present(): PlayerUiModel {
        val navigator = LocalScreenNavigator.current
        val context = LocalContext.current
        val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
        val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
        val playerState by soundEngine.state.collectAsState()

        fun currentStep() = (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 10f / maxVolume).roundToInt()

        // Shown as a plain 0-10 click count rather than a percentage, regardless of how many
        // steps this device's STREAM_MUSIC actually has.
        var volumeStep by remember { mutableIntStateOf(currentStep()) }
        var volumeChangeTick by remember { mutableIntStateOf(0) }
        var volumeIndicatorVisible by remember { mutableStateOf(false) }

        // VOLUME_CHANGED_ACTION can be deferred by several seconds on modern Android for
        // battery reasons, which made the overlay miss hardware-rocker changes entirely.
        // Polling is cheap and catches those (and other apps') changes within ~150ms.
        LaunchedEffect(Unit) {
            while (true) {
                delay(VOLUME_POLL_INTERVAL_MS)
                val latest = currentStep()
                if (latest != volumeStep) {
                    volumeStep = latest
                    volumeChangeTick++
                }
            }
        }

        LaunchedEffect(volumeChangeTick) {
            if (volumeChangeTick == 0) return@LaunchedEffect
            volumeIndicatorVisible = true
            delay(900)
            volumeIndicatorVisible = false
        }

        return PlayerUiModel(
            soundType = playerState.type,
            isPlaying = playerState.isPlaying,
            volumeStep = volumeStep,
            volumeIndicatorVisible = volumeIndicatorVisible,
            eventHandler = EventHandler { event ->
                when (event) {
                    ChooseSoundClicked -> navigator.goToPicker()
                    PlayPauseClicked -> soundEngine.togglePlayPause()
                    AboutClicked -> navigator.goToAbout()
                    is VolumeDragged -> {
                        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        val newVolume = (current + event.steps).coerceIn(0, maxVolume)
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                        volumeStep = currentStep()
                        volumeChangeTick++
                    }
                }
            },
        )
    }
}
