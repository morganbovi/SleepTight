package com.apkrocket.sleeptight.features.player

import com.apkrocket.sleeptight.audio.SoundType
import com.apkrocket.sleeptight.ui.presenter.EventHandler

data class PlayerUiModel(
    val soundType: SoundType?,
    val isPlaying: Boolean,
    /** 0-10 click count, not a percentage. */
    val volumeStep: Int,
    val volumeIndicatorVisible: Boolean,
    val eventHandler: EventHandler<Event>,
) {
    sealed interface Event {
        data object ChooseSoundClicked : Event
        data object PlayPauseClicked : Event
        data object AboutClicked : Event
        data class VolumeDragged(val steps: Int) : Event
    }
}
