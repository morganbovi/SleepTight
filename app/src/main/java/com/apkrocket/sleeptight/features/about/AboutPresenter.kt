package com.apkrocket.sleeptight.features.about

import androidx.compose.runtime.Composable
import com.apkrocket.sleeptight.features.about.AboutUiModel.Event.BackClicked
import com.apkrocket.sleeptight.ui.navigation.LocalScreenNavigator
import com.apkrocket.sleeptight.ui.presenter.EventHandler

class AboutPresenter {

    @Composable
    fun present(): AboutUiModel {
        val navigator = LocalScreenNavigator.current

        return AboutUiModel(
            eventHandler = EventHandler { event ->
                when (event) {
                    BackClicked -> navigator.goToPlayer()
                }
            },
        )
    }
}
