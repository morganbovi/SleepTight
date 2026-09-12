package com.apkrocket.sleeptight.features.picker

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.apkrocket.sleeptight.audio.SoundEngine
import com.apkrocket.sleeptight.audio.SoundType
import com.apkrocket.sleeptight.data.LastSoundRepository
import com.apkrocket.sleeptight.features.picker.SoundPickerUiModel.Event.AboutClicked
import com.apkrocket.sleeptight.features.picker.SoundPickerUiModel.Event.BackClicked
import com.apkrocket.sleeptight.features.picker.SoundPickerUiModel.Event.PlayPauseClicked
import com.apkrocket.sleeptight.features.picker.SoundPickerUiModel.Event.SoundClicked
import com.apkrocket.sleeptight.ui.navigation.LocalScreenNavigator
import com.apkrocket.sleeptight.ui.presenter.EventHandler

class SoundPickerPresenter(
    private val soundEngine: SoundEngine = SoundEngine,
) {

    @Composable
    fun present(): SoundPickerUiModel {
        val navigator = LocalScreenNavigator.current
        val context = LocalContext.current
        val lastSoundRepository = remember { LastSoundRepository(context) }
        val playerState by soundEngine.state.collectAsState()
        val hasActiveSound = playerState.type != null

        // Mirrors the visible back arrow: nothing to go back to at a fresh startup.
        BackHandler(enabled = hasActiveSound) {
            navigator.goToPlayer()
        }

        return SoundPickerUiModel(
            sounds = SoundType.entries,
            showBackButton = hasActiveSound,
            activeSoundType = playerState.type,
            isPlaying = playerState.isPlaying,
            eventHandler = EventHandler { event ->
                when (event) {
                    is SoundClicked -> {
                        soundEngine.play(event.type)
                        lastSoundRepository.save(event.type)
                        navigator.goToPlayer()
                    }
                    BackClicked -> navigator.goToPlayer()
                    PlayPauseClicked -> soundEngine.togglePlayPause()
                    AboutClicked -> navigator.goToAbout()
                }
            },
        )
    }
}
