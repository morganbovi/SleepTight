package com.apkrocket.sleeptight.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkrocket.sleeptight.audio.SoundEngine
import com.apkrocket.sleeptight.data.LastSoundRepository
import com.apkrocket.sleeptight.features.about.AboutScreen
import com.apkrocket.sleeptight.features.picker.SoundPickerScreen
import com.apkrocket.sleeptight.features.player.PlayerScreen
import com.apkrocket.sleeptight.service.PlaybackServicePresenter
import com.apkrocket.sleeptight.ui.navigation.LocalScreenNavigator
import com.apkrocket.sleeptight.ui.navigation.Screen
import com.apkrocket.sleeptight.ui.navigation.ScreenNavigator

@Composable
fun SleepTightApp() {
    val context = LocalContext.current
    val lastSoundRepository = remember { LastSoundRepository(context) }

    // On a fresh start, reopen straight into the last sound; with nothing remembered,
    // there's nothing to show on the player, so go straight to the picker instead.
    var screen by remember {
        mutableStateOf(
            lastSoundRepository.load()?.let { lastSound ->
                SoundEngine.restore(lastSound)
                Screen.PLAYER
            } ?: Screen.PICKER
        )
    }

    val navigator = remember {
        object : ScreenNavigator {
            override fun goToPlayer() {
                screen = Screen.PLAYER
            }

            override fun goToPicker() {
                screen = Screen.PICKER
            }

            override fun goToAbout() {
                screen = Screen.ABOUT
            }
        }
    }

    remember { PlaybackServicePresenter() }.present()

    CompositionLocalProvider(LocalScreenNavigator provides navigator) {
        when (screen) {
            Screen.PLAYER -> PlayerScreen()
            Screen.PICKER -> SoundPickerScreen()
            Screen.ABOUT -> AboutScreen()
        }
    }
}
