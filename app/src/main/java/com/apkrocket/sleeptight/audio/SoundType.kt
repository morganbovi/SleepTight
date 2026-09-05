package com.apkrocket.sleeptight.audio

/** Every ambient sound the mixer can play, each synthesized on-device — no bundled audio files. */
enum class SoundType(val label: String) {
    WHITE_NOISE("White Noise"),
    PINK_NOISE("Pink Noise"),
    BROWN_NOISE("Brown Noise"),
    RAIN("Rain"),
    OCEAN_WAVES("Ocean Waves"),
    WIND("Wind"),
    CAMPFIRE("Campfire"),
    HEARTBEAT("Heartbeat"),
}
