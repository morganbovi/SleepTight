package com.apkrocket.sleeptight.features.about

import com.apkrocket.sleeptight.ui.presenter.EventHandler

data class AboutUiModel(
    val eventHandler: EventHandler<Event>,
) {
    sealed interface Event {
        data object BackClicked : Event
    }
}
