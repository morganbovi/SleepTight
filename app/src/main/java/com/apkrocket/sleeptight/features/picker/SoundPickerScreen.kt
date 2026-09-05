package com.apkrocket.sleeptight.features.picker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkrocket.sleeptight.audio.SoundType
import com.apkrocket.sleeptight.features.picker.SoundPickerUiModel.Event.BackClicked
import com.apkrocket.sleeptight.features.picker.SoundPickerUiModel.Event.SoundClicked
import com.apkrocket.sleeptight.ui.background.defaultSoundPalette
import com.apkrocket.sleeptight.ui.background.soundPalettes
import com.apkrocket.sleeptight.ui.icons.icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundPickerScreen(
    presenter: SoundPickerPresenter = remember { SoundPickerPresenter() },
) {
    val uiModel = presenter.present()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose a Sound", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    if (uiModel.showBackButton) {
                        IconButton(onClick = { uiModel.eventHandler(BackClicked) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uiModel.sounds) { type ->
                SoundCard(type = type, onClick = { uiModel.eventHandler(SoundClicked(type)) })
            }
        }
    }
}

@Composable
private fun SoundCard(type: SoundType, onClick: () -> Unit) {
    val palette = soundPalettes[type] ?: defaultSoundPalette

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(palette.top, palette.bottom)))
            .clickable(onClick = onClick),
    ) {
        // Faint starfield texture ties every card to the launcher icon's background art.
        val stars = remember(type) {
            val rnd = kotlin.random.Random(type.ordinal * 91 + 7)
            List(10) { Triple(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat() * 0.5f + 0.2f) }
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEach { (fx, fy, alpha) ->
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = 1.4f,
                    center = Offset(fx * size.width, fy * size.height)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                // Soft glow behind the icon, matching the launcher icon's moon glow.
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(palette.accent.copy(alpha = 0.35f), palette.accent.copy(alpha = 0f))
                            ),
                            shape = RoundedCornerShape(50)
                        )
                )
                Icon(
                    imageVector = type.icon(),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            Text(
                text = type.label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
