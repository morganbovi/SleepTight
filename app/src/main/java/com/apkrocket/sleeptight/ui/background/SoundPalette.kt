package com.apkrocket.sleeptight.ui.background

import androidx.compose.ui.graphics.Color
import com.apkrocket.sleeptight.audio.SoundType

/** Mood colors for one sound, shared by the full-screen player background and the picker cards. */
class SoundPalette(val top: Color, val bottom: Color, val accent: Color)

val soundPalettes = mapOf(
    SoundType.WHITE_NOISE to SoundPalette(Color(0xFF3A3F4E), Color(0xFF14161D), Color(0xFFC7CEE0)),
    SoundType.PINK_NOISE to SoundPalette(Color(0xFF473852), Color(0xFF1B1420), Color(0xFFECBEE0)),
    SoundType.BROWN_NOISE to SoundPalette(Color(0xFF4A3420), Color(0xFF1B120A), Color(0xFFD79A5C)),
    SoundType.RAIN to SoundPalette(Color(0xFF29384C), Color(0xFF0A0D14), Color(0xFF8FB4D6)),
    SoundType.OCEAN_WAVES to SoundPalette(Color(0xFF15495F), Color(0xFF051019), Color(0xFF5FC2DE)),
    SoundType.WIND to SoundPalette(Color(0xFF37474F), Color(0xFF11161A), Color(0xFFD7E3E8)),
    SoundType.CAMPFIRE to SoundPalette(Color(0xFF45230F), Color(0xFF130704), Color(0xFFFF9248)),
    SoundType.HEARTBEAT to SoundPalette(Color(0xFF4A1E30), Color(0xFF160810), Color(0xFFFF7A9A)),
)

val defaultSoundPalette = SoundPalette(Color(0xFF10131F), Color(0xFF05060A), Color(0xFF6E7BB8))
