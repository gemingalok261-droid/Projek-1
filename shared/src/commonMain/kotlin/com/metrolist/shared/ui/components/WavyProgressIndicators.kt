/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.shared.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.loading
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun AccretionLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 64.dp,
    label: String = stringResource(Res.string.loading),
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AccretionDiskLoader")

    // Outer orbiting rings rotation
    val spin1Angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spin1",
    )
    val spin2Angle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spin2",
    )

    // Flowing dash offset for relativistic stream
    val flowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "flowPhase",
    )

    // Equalizer wave phase
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wavePhase",
    )

    // Event horizon core & halo pulse
    val pulseFactor by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseFactor",
    )

    // Gravitational infalling particles
    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "particleProgress",
    )

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                contentDescription = label
                liveRegion = LiveRegionMode.Polite
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size.minDimension
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val scale = canvasSize / 512f

            // 1. Outer Orbiting Ring 1 (spin1 clockwise)
            rotate(degrees = spin1Angle, pivot = Offset(cx, cy)) {
                val r1 = 186f * scale
                val dash1 = PathEffect.dashPathEffect(
                    floatArrayOf(60f * scale, 10f * scale, 6f * scale, 10f * scale, 2f * scale, 10f * scale),
                    phase = 0f,
                )
                drawCircle(
                    color = color.copy(alpha = 0.45f),
                    radius = r1,
                    center = Offset(cx, cy),
                    style = Stroke(width = maxOf(1f, 1.5f * scale), pathEffect = dash1),
                )
            }

            // 2. Outer Orbiting Ring 2 (spin2 counter-clockwise)
            rotate(degrees = spin2Angle, pivot = Offset(cx, cy)) {
                val r2 = 178f * scale
                val dash2 = PathEffect.dashPathEffect(
                    floatArrayOf(3f * scale, 9f * scale),
                    phase = 0f,
                )
                drawCircle(
                    color = color.copy(alpha = 0.30f),
                    radius = r2,
                    center = Offset(cx, cy),
                    style = Stroke(width = maxOf(1f, 1.0f * scale), pathEffect = dash2),
                )
            }

            // 3. 56 Radial Equalizer Bars
            val barCount = 56
            val rOuter = 236f * scale
            val baseBarHeight = 40f * scale
            val barWidth = maxOf(1.5f, 5f * scale)
            val strokeCap = StrokeCap.Round

            for (i in 0 until barCount) {
                val angleDeg = i * (360f / barCount)
                val angleRad = (angleDeg * PI / 180f).toFloat()

                // Harmonic soundwave modulation around the circle
                val wave1 = sin(wavePhase * 3f + i * 0.45f)
                val wave2 = cos(wavePhase * 2f - i * 0.32f)
                val barScale = 0.25f + 0.75f * ((wave1 * 0.65f + wave2 * 0.35f + 1f) / 2f).coerceIn(0f, 1f)
                val currentBarHeight = baseBarHeight * barScale

                val startDist = rOuter - currentBarHeight
                val endDist = rOuter

                val xStart = cx + startDist * sin(angleRad)
                val yStart = cy - startDist * cos(angleRad)
                val xEnd = cx + endDist * sin(angleRad)
                val yEnd = cy - endDist * cos(angleRad)

                val barAlpha = (0.55f + 0.45f * barScale).coerceIn(0f, 1f)
                drawLine(
                    color = color.copy(alpha = barAlpha),
                    start = Offset(xStart, yStart),
                    end = Offset(xEnd, yEnd),
                    strokeWidth = barWidth,
                    cap = strokeCap,
                )
            }

            // 4. Tilted Accretion Disk (-28 degrees tilt) - Back arcs
            rotate(degrees = -28f, pivot = Offset(cx, cy)) {
                val dashFlow1 = PathEffect.dashPathEffect(
                    floatArrayOf(120f * scale, 10f * scale, 50f * scale, 10f * scale, 8f * scale, 10f * scale),
                    phase = -flowPhase * scale,
                )
                val dashFlow2 = PathEffect.dashPathEffect(
                    floatArrayOf(40f * scale, 8f * scale, 14f * scale, 8f * scale),
                    phase = flowPhase * 1.5f * scale,
                )
                val dashFlow3 = PathEffect.dashPathEffect(
                    floatArrayOf(90f * scale, 14f * scale, 30f * scale, 14f * scale, 4f * scale, 14f * scale),
                    phase = flowPhase * 0.7f * scale,
                )

                // Outer stream back arc
                drawArc(
                    color = color.copy(alpha = 0.50f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - 168f * scale, cy - 46f * scale),
                    size = Size(336f * scale, 92f * scale),
                    style = Stroke(width = maxOf(1f, 1.5f * scale), pathEffect = dashFlow3),
                )
                // Relativistic main stream back arc
                drawArc(
                    color = color.copy(alpha = 0.85f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - 148f * scale, cy - 39f * scale),
                    size = Size(296f * scale, 78f * scale),
                    style = Stroke(width = maxOf(2f, 7f * scale), pathEffect = dashFlow1),
                )
                // Inner stream back arc
                drawArc(
                    color = color.copy(alpha = 0.70f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - 128f * scale, cy - 32f * scale),
                    size = Size(256f * scale, 64f * scale),
                    style = Stroke(width = maxOf(1.5f, 3f * scale), pathEffect = dashFlow2),
                )
            }

            // 5. Central Black Hole Event Horizon & Singularity Core
            // Outer glowing halo
            val haloRadius = 76f * scale
            drawCircle(
                color = color.copy(alpha = (0.35f * pulseFactor).coerceIn(0f, 1f)),
                radius = haloRadius,
                center = Offset(cx, cy),
                style = Stroke(width = maxOf(1.5f, 2.5f * scale)),
            )

            // Singularity Core (Pitch Black Void)
            val coreRadius = 62f * scale
            drawCircle(
                color = Color.Black,
                radius = coreRadius,
                center = Offset(cx, cy),
            )
            // Core luminous event horizon rim
            drawCircle(
                color = color.copy(alpha = (0.75f + 0.25f * pulseFactor).coerceIn(0f, 1f)),
                radius = coreRadius,
                center = Offset(cx, cy),
                style = Stroke(width = maxOf(2f, 3.5f * scale)),
            )

            // 6. Front half arcs of the accretion disk (passing in FRONT of the black hole horizon)
            rotate(degrees = -28f, pivot = Offset(cx, cy)) {
                val dashFlow1 = PathEffect.dashPathEffect(
                    floatArrayOf(120f * scale, 10f * scale, 50f * scale, 10f * scale, 8f * scale, 10f * scale),
                    phase = -flowPhase * scale,
                )
                val dashFlow2 = PathEffect.dashPathEffect(
                    floatArrayOf(40f * scale, 8f * scale, 14f * scale, 8f * scale),
                    phase = flowPhase * 1.5f * scale,
                )
                val dashFlow3 = PathEffect.dashPathEffect(
                    floatArrayOf(90f * scale, 14f * scale, 30f * scale, 14f * scale, 4f * scale, 14f * scale),
                    phase = flowPhase * 0.7f * scale,
                )

                // Outer stream front arc
                drawArc(
                    color = color.copy(alpha = 0.55f),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - 168f * scale, cy - 46f * scale),
                    size = Size(336f * scale, 92f * scale),
                    style = Stroke(width = maxOf(1f, 1.5f * scale), pathEffect = dashFlow3),
                )
                // Relativistic main stream front arc
                drawArc(
                    color = color.copy(alpha = 0.95f),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - 148f * scale, cy - 39f * scale),
                    size = Size(296f * scale, 78f * scale),
                    style = Stroke(width = maxOf(2f, 7f * scale), pathEffect = dashFlow1),
                )
                // Inner stream front arc
                drawArc(
                    color = color.copy(alpha = 0.80f),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - 128f * scale, cy - 32f * scale),
                    size = Size(256f * scale, 64f * scale),
                    style = Stroke(width = maxOf(1.5f, 3f * scale), pathEffect = dashFlow2),
                )
            }

            // 7. Infalling particles / quantum notes spiraling into the singularity
            val particleCount = 4
            for (p in 0 until particleCount) {
                val prog = (particleProgress + p * (1f / particleCount)) % 1f
                val dist = (1f - prog.pow(1.6f)) * 145f * scale + 6f * scale
                val theta = prog * 3f * PI.toFloat() + p * (PI.toFloat() / 2f)
                val px = cx + dist * cos(theta)
                val py = cy + dist * sin(theta) * 0.6f

                val pAlpha = when {
                    prog < 0.15f -> prog / 0.15f
                    prog > 0.85f -> (1f - prog) / 0.15f
                    else -> 0.95f
                }
                val particleRadius = maxOf(1f, (1f - prog * 0.7f) * 3.5f * scale)

                drawCircle(
                    color = color.copy(alpha = pAlpha.coerceIn(0f, 1f)),
                    radius = particleRadius,
                    center = Offset(px, py),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContainedLoadingIndicator(
    modifier: Modifier = Modifier,
    containerColor: Color = LoadingIndicatorDefaults.containedContainerColor,
    indicatorColor: Color = LoadingIndicatorDefaults.containedIndicatorColor,
    label: String = stringResource(Res.string.loading),
) {
    AccretionLoadingIndicator(
        modifier = modifier,
        color = indicatorColor,
        size = 48.dp,
        label = label,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContainedLoadingIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    containerColor: Color = LoadingIndicatorDefaults.containedContainerColor,
    indicatorColor: Color = LoadingIndicatorDefaults.containedIndicatorColor,
    label: String = stringResource(Res.string.loading),
) {
    AccretionLoadingIndicator(
        modifier = modifier,
        color = indicatorColor,
        size = 48.dp,
        label = label,
    )
}

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    label: String = stringResource(Res.string.loading),
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AccretionLoadingIndicator(
            size = 96.dp,
            label = label,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WavyCircularProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    animate: Boolean = true,
) {
    CircularWavyProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        waveSpeed = if (animate) WavyProgressIndicatorDefaults.CircularWavelength else 0.dp,
    )
}
