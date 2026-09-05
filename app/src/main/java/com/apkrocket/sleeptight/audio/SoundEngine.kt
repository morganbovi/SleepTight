package com.apkrocket.sleeptight.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** What the player is doing right now: at most one sound, playing or paused. */
data class PlayerState(
    val type: SoundType? = null,
    val isPlaying: Boolean = false,
)

/**
 * App-wide singleton owning the single currently-selected sound. Deliberately outside any
 * Android component lifecycle so playback survives Activity recreation; [PlaybackService]
 * merely keeps the process alive and observes [state] to drive its notification.
 *
 * Loudness is not managed here — every voice plays at full internal gain and the actual
 * volume the user hears is the device's STREAM_MUSIC volume (see PlayerPresenter).
 */
object SoundEngine {
    private var voice: SoundVoice? = null

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state

    /** Loads a remembered sound as selected-but-paused, e.g. on a fresh app start. Never overrides a live session. */
    fun restore(type: SoundType) {
        if (_state.value.type != null) return
        _state.value = PlayerState(type = type, isPlaying = false)
    }

    fun play(type: SoundType) {
        if (_state.value.type == type && _state.value.isPlaying) return
        voice?.stop()
        voice = SoundVoice(type).also { it.start() }
        _state.update { it.copy(type = type, isPlaying = true) }
    }

    fun togglePlayPause() {
        val current = _state.value.type ?: return
        if (_state.value.isPlaying) {
            voice?.stop()
            voice = null
            _state.update { it.copy(isPlaying = false) }
        } else {
            voice = SoundVoice(current).also { it.start() }
            _state.update { it.copy(isPlaying = true) }
        }
    }

    fun stop() {
        voice?.stop()
        voice = null
        _state.value = PlayerState()
    }
}
