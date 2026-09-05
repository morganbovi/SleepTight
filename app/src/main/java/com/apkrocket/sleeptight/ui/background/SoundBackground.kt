package com.apkrocket.sleeptight.ui.background

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.apkrocket.sleeptight.audio.SoundType
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Full-bleed animated art standing in for a photo, generated at runtime — same reasoning as the synthesized audio: no stock imagery to license. */
@Composable
fun SoundBackground(type: SoundType?, modifier: Modifier = Modifier) {
    val palette = type?.let { soundPalettes[it] } ?: defaultSoundPalette
    val transition = rememberInfiniteTransition(label = "bg")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationFor(type), easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    val grainDots = remember(type) {
        val rnd = Random(type?.ordinal ?: -1)
        List(90) { Triple(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat()) }
    }

    Canvas(
        modifier = modifier.background(Brush.verticalGradient(listOf(palette.top, palette.bottom)))
    ) {
        when (type) {
            SoundType.RAIN -> drawRain(phase, palette.accent)
            SoundType.OCEAN_WAVES -> drawWaves(phase, palette.accent)
            SoundType.WIND -> drawWind(phase, palette.accent)
            SoundType.CAMPFIRE -> drawEmbers(phase, palette.accent)
            SoundType.HEARTBEAT -> drawPulse(phase, palette.accent)
            SoundType.WHITE_NOISE, SoundType.PINK_NOISE, SoundType.BROWN_NOISE -> drawGrain(phase, palette.accent, grainDots)
            null -> Unit
        }
    }
}

private fun durationFor(type: SoundType?): Int = when (type) {
    SoundType.RAIN -> 1400
    SoundType.OCEAN_WAVES -> 7000
    SoundType.WIND -> 5000
    SoundType.CAMPFIRE -> 2200
    SoundType.HEARTBEAT -> 1100
    else -> 3000
}

private fun DrawScope.drawRain(phase: Float, accent: Color) {
    val columns = 22
    val slant = size.width * 0.06f
    for (i in 0 until columns) {
        val baseX = size.width * (i.toFloat() / columns) + (i % 3) * 9f
        val length = size.height * 0.09f
        val travel = (phase + i * 0.13f) % 1f
        val startY = -length + travel * (size.height + length)
        drawLine(
            color = accent.copy(alpha = 0.35f),
            start = Offset(baseX, startY),
            end = Offset(baseX - slant, startY + length),
            strokeWidth = 2.5f
        )
    }
}

private fun DrawScope.drawWaves(phase: Float, accent: Color) {
    val layers = 3
    for (layer in 0 until layers) {
        val amplitude = size.height * (0.02f + layer * 0.012f)
        val baseline = size.height * (0.62f + layer * 0.1f)
        val wavelength = size.width / (1.3f + layer * 0.4f)
        val shift = phase * 2f * PI.toFloat() * (if (layer % 2 == 0) 1f else -1f)

        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, baseline)
            var x = 0f
            while (x <= size.width) {
                val y = baseline + amplitude * sin((x / wavelength) * 2 * PI.toFloat() + shift)
                lineTo(x, y)
                x += 8f
            }
            lineTo(size.width, size.height)
            close()
        }
        drawPath(path, color = accent.copy(alpha = 0.16f + layer * 0.08f))
    }
}

private fun DrawScope.drawWind(phase: Float, accent: Color) {
    val streaks = 5
    for (i in 0 until streaks) {
        val baseY = size.height * (0.2f + i * 0.15f)
        val amplitude = size.height * 0.03f
        val speed = 1f + i * 0.4f
        val shift = ((phase * speed) % 1f)

        val path = Path()
        var started = false
        var x = -size.width * 0.3f
        while (x <= size.width * 1.3f) {
            val progress = (x / size.width) + shift
            val y = baseY + amplitude * sin(progress * 2 * PI.toFloat())
            if (!started) {
                path.moveTo(x, y); started = true
            } else {
                path.lineTo(x, y)
            }
            x += 10f
        }
        drawPath(path, color = accent.copy(alpha = 0.22f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
    }
}

private fun DrawScope.drawEmbers(phase: Float, accent: Color) {
    val glowRadius = size.minDimension * (0.35f + 0.05f * sin(phase * 2 * PI.toFloat()))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(accent.copy(alpha = 0.45f), accent.copy(alpha = 0f)),
            center = Offset(size.width / 2f, size.height * 0.78f),
            radius = glowRadius
        ),
        radius = glowRadius,
        center = Offset(size.width / 2f, size.height * 0.78f)
    )

    val particles = 14
    for (i in 0 until particles) {
        val seed = i * 37
        val travel = (phase + (seed % 100) / 100f) % 1f
        val x = size.width * (0.3f + 0.4f * ((seed % 7) / 7f))
        val y = size.height * 0.75f - travel * size.height * 0.55f
        val alpha = (1f - travel) * 0.6f
        drawCircle(color = accent.copy(alpha = alpha.coerceIn(0f, 0.6f)), radius = 3.5f, center = Offset(x, y))
    }
}

private fun DrawScope.drawPulse(phase: Float, accent: Color) {
    val center = Offset(size.width / 2f, size.height * 0.5f)
    val maxRadius = size.minDimension * 0.55f
    val rings = 3
    for (r in 0 until rings) {
        val ringPhase = (phase + r / rings.toFloat()) % 1f
        val radius = maxRadius * ringPhase
        val alpha = (1f - ringPhase) * 0.35f
        drawCircle(color = accent.copy(alpha = alpha), radius = radius, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
    }
}

private fun DrawScope.drawGrain(phase: Float, accent: Color, dots: List<Triple<Float, Float, Float>>) {
    dots.forEachIndexed { index, (fx, fy, seedAlpha) ->
        val shimmer = 0.5f + 0.5f * sin((phase * 2 * PI.toFloat()) + index)
        val offset = Offset(fx * size.width, fy * size.height)
        drawCircle(color = accent.copy(alpha = (seedAlpha * shimmer * 0.35f)), radius = 1.8f, center = offset)
    }
}
