package com.apkrocket.sleeptight.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.apkrocket.sleeptight.audio.SoundType

/** A recognizable glyph for each synthesized sound, standing in for a photo the same way the animated backgrounds do. */
fun SoundType.icon(): ImageVector = when (this) {
    SoundType.WHITE_NOISE -> Icons.Filled.Radio
    SoundType.PINK_NOISE -> Icons.Filled.Cloud
    SoundType.BROWN_NOISE -> Icons.Filled.GraphicEq
    SoundType.RAIN -> Icons.Filled.WaterDrop
    SoundType.OCEAN_WAVES -> Icons.Filled.Waves
    SoundType.WIND -> Icons.Filled.Air
    SoundType.CAMPFIRE -> Icons.Filled.LocalFireDepartment
    SoundType.HEARTBEAT -> Icons.Filled.MonitorHeart
}
