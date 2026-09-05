package com.apkrocket.sleeptight.features.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkrocket.sleeptight.features.player.PlayerUiModel.Event.AboutClicked
import com.apkrocket.sleeptight.features.player.PlayerUiModel.Event.ChooseSoundClicked
import com.apkrocket.sleeptight.features.player.PlayerUiModel.Event.PlayPauseClicked
import com.apkrocket.sleeptight.features.player.PlayerUiModel.Event.VolumeDragged
import com.apkrocket.sleeptight.ui.background.SoundBackground

/** Drag distance per volume step — small enough that a short flick moves several notches. */
private const val VOLUME_STEP_DP = 24

@Composable
fun PlayerScreen(
    presenter: PlayerPresenter = remember { PlayerPresenter() },
) {
    val uiModel = presenter.present()

    PlayerScreenContent(uiModel = uiModel)
}

@Composable
private fun PlayerScreenContent(uiModel: PlayerUiModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(uiModel.soundType) {
                if (uiModel.soundType == null) return@pointerInput
                val stepPx = VOLUME_STEP_DP.dp.toPx()
                var accumulatedDrag = 0f
                detectVerticalDragGestures(
                    onDragStart = { accumulatedDrag = 0f },
                    onDragEnd = { accumulatedDrag = 0f },
                    onDragCancel = { accumulatedDrag = 0f },
                ) { change, dragAmount ->
                    change.consume()
                    accumulatedDrag += dragAmount
                    while (accumulatedDrag <= -stepPx) {
                        uiModel.eventHandler(VolumeDragged(steps = 1))
                        accumulatedDrag += stepPx
                    }
                    while (accumulatedDrag >= stepPx) {
                        uiModel.eventHandler(VolumeDragged(steps = -1))
                        accumulatedDrag -= stepPx
                    }
                }
            }
    ) {
        SoundBackground(type = uiModel.soundType, modifier = Modifier.fillMaxSize())

        // Scrim so text/controls stay legible over any background art.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.35f),
                        0.5f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.55f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Text(
                text = uiModel.soundType?.label ?: "Choose a sound",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (uiModel.soundType == null) {
                Text(
                    text = "Tap \"Sounds\" below to start",
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = uiModel.volumeIndicatorVisible,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            VolumeFillOverlay(step = uiModel.volumeStep, modifier = Modifier.fillMaxSize())
        }

        PlayerControlBar(
            uiModel = uiModel,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 32.dp)
        )
    }
}

/**
 * The whole screen fills from the bottom like rising water, easing toward each new level
 * rather than snapping — the click count still drives it, but the motion reads as continuous.
 */
@Composable
private fun VolumeFillOverlay(step: Int, modifier: Modifier = Modifier) {
    val fraction by animateFloatAsState(
        targetValue = (step / 10f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "volumeFill"
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(fraction)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0f), Color.White.copy(alpha = 0.24f))
                    )
                )
        )
        Text(
            "$step",
            color = Color.White,
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/** One cohesive bar for every transport control, instead of separate floating shapes. */
@Composable
private fun PlayerControlBar(uiModel: PlayerUiModel, modifier: Modifier = Modifier) {
    val hasSound = uiModel.soundType != null

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControlBarItem(
            icon = Icons.Filled.GridView,
            label = "Sounds",
            onClick = { uiModel.eventHandler(ChooseSoundClicked) }
        )

        PlayPauseButton(
            isPlaying = uiModel.isPlaying,
            enabled = hasSound,
            onClick = { uiModel.eventHandler(PlayPauseClicked) }
        )

        ControlBarItem(
            icon = Icons.Filled.Info,
            label = "About",
            onClick = { uiModel.eventHandler(AboutClicked) }
        )
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
