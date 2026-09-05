package com.apkrocket.sleeptight.features.picker

import com.apkrocket.sleeptight.audio.SoundType
import com.apkrocket.sleeptight.ui.presenter.EventHandler

data class SoundPickerUiModel(
    val sounds: List<SoundType>,
    val showBackButton: Boolean,
    val eventHandler: EventHandler<Event>,
) {
    sealed interface Event {
        data class SoundClicked(val type: SoundType) : Event
        data object BackClicked : Event
    }
}
