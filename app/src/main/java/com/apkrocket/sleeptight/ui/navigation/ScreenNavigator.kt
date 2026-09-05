package com.apkrocket.sleeptight.ui.navigation

import androidx.compose.runtime.compositionLocalOf

enum class Screen { PLAYER, PICKER, ABOUT }

interface ScreenNavigator {
    fun goToPlayer()
    fun goToPicker()
    fun goToAbout()
}

val LocalScreenNavigator = compositionLocalOf<ScreenNavigator> {
    error("No ScreenNavigator provided")
}
