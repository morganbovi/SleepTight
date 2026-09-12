package com.apkrocket.sleeptight.features.about

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.apkrocket.sleeptight.features.about.AboutUiModel.Event.BackClicked
import com.apkrocket.sleeptight.ui.navigation.LocalScreenNavigator
import com.apkrocket.sleeptight.ui.presenter.EventHandler

class AboutPresenter {

    @Composable
    fun present(): AboutUiModel {
        val navigator = LocalScreenNavigator.current

        BackHandler {
            navigator.goToPlayer()
        }

        return AboutUiModel(
            eventHandler = EventHandler { event ->
                when (event) {
                    BackClicked -> navigator.goToPlayer()
                }
            },
        )
    }
}
