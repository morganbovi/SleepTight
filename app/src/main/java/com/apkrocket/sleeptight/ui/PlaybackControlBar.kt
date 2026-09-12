package com.apkrocket.sleeptight.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkrocket.sleeptight.audio.SoundType
import com.apkrocket.sleeptight.ui.background.defaultSoundPalette
import com.apkrocket.sleeptight.ui.background.soundPalettes
import com.apkrocket.sleeptight.ui.icons.icon

/** What the left slot of [PlaybackControlBar] shows — contextual to which screen it's on. */
sealed interface ControlBarLeftSlot {
    data object ChooseSound : ControlBarLeftSlot
    data class NowPlaying(val type: SoundType) : ControlBarLeftSlot
}

/**
 * One cohesive bar for every transport control, shared by the Player and Picker screens so
 * playback stays reachable and controllable no matter which one you're looking at.
 */
@Composable
fun PlaybackControlBar(
    leftSlot: ControlBarLeftSlot,
    isPlaying: Boolean,
    playPauseEnabled: Boolean,
    onLeftSlotClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (leftSlot) {
            ControlBarLeftSlot.ChooseSound ->
                ControlBarItem(icon = Icons.Filled.GridView, label = "Sounds", onClick = onLeftSlotClick)
            is ControlBarLeftSlot.NowPlaying ->
                NowPlayingChip(type = leftSlot.type, onClick = onLeftSlotClick)
        }

        PlayPauseButton(
            isPlaying = isPlaying,
            enabled = playPauseEnabled,
            onClick = onPlayPauseClick
        )

        ControlBarItem(icon = Icons.Filled.Info, label = "About", onClick = onAboutClick)
    }
}

@Composable
private fun ControlBarItem(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val alpha = if (enabled) 1f else 0.35f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(
                if (enabled) Modifier.pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) } else Modifier
            )
            .padding(horizontal = 20.dp, vertical = 2.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color.White.copy(alpha = alpha), modifier = Modifier.size(26.dp))
        Text(label, color = Color.White.copy(alpha = alpha), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

/** Stands in for the "Sounds" item when you're already on the picker with something playing. */
@Composable
private fun NowPlayingChip(type: SoundType, onClick: () -> Unit) {
    val palette = soundPalettes[type] ?: defaultSoundPalette
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(palette.accent.copy(alpha = 0.28f))
            .pointerInput(type) { detectTapGestures(onTap = { onClick() }) }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(type.icon(), contentDescription = null, tint = palette.accent, modifier = Modifier.size(18.dp))
        Text(
            type.label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun PlayPauseButton(isPlaying: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val alpha = if (enabled) 1f else 0.35f
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = alpha))
            .then(
                if (enabled) Modifier.pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) } else Modifier
            )
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.Black.copy(alpha = if (enabled) 0.85f else 0.4f),
            modifier = Modifier.size(30.dp)
        )
    }
}
