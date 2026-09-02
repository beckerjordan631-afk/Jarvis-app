package com.jarvis.assistant.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.ui.theme.JarvisBlue
import com.jarvis.assistant.ui.theme.JarvisBlueBright
import com.jarvis.assistant.ui.theme.JarvisGreen
import com.jarvis.assistant.ui.theme.JarvisRed
import com.jarvis.assistant.viewmodel.JarvisState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisOrb(
    state: JarvisState,
    modifier: Modifier = Modifier,
    sizeDp: Int = 220
) {
    val transition = rememberInfiniteTransition(label = "orb")

    val speedMs = when (state) {
        JarvisState.IDLE -> 12000
        JarvisState.LISTENING -> 4000
        JarvisState.THINKING -> 1800
        JarvisState.SPEAKING -> 2600
    }

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(speedMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = if (state == JarvisState.IDLE) 1.0f else 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                if (state == JarvisState.SPEAKING) 350 else 1400,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val coreColor = when (state) {
        JarvisState.IDLE -> JarvisBlue
        JarvisState.LISTENING -> JarvisBlueBright
        JarvisState.THINKING -> JarvisGreen
        JarvisState.SPEAKING -> JarvisBlueBright
    }

    Canvas(
        modifier = modifier.size(sizeDp.dp)
    ) {
        val radius = size.minDimension / 2f * pulse
        val center = Offset(
            size.width / 2f,
            size.height / 2f
        )

        val rings = 5
        val pointsPerRing = 14

        for (r in 1 until rings) {
            val ringRadius = radius * (r / rings.toFloat())
            val squash = 0.35f + 0.5f * (r / rings.toFloat())

            drawSpherePath(
                center = center,
                radius = ringRadius,
                verticalSquash = squash,
                rotationDeg = rotation,
                color = coreColor.copy(alpha = 0.55f)
            )
        }

        for (i in 0 until pointsPerRing) {
            val angle = Math.toRadians(
                (rotation + i * (360f / pointsPerRing)).toDouble()
            )

            val x = center.x +
                    (radius * 0.9f) *
                    cos(angle).toFloat()

            val y = center.y +
                    (radius * 0.5f) *
                    sin(angle).toFloat()

            drawCircle(
                color = coreColor,
                radius = 3f,
                center = Offset(x, y)
            )
        }

        drawCircle(
            color = coreColor,
            radius = radius * 0.12f,
            center = center
        )

        drawCircle(
            color = coreColor.copy(alpha = 0.25f),
            radius = radius * 0.32f,
            center = center,
            style = Stroke(width = 2f)
        )

        if (state == JarvisState.THINKING) {
            drawCircle(
                color = JarvisRed.copy(alpha = 0f),
                radius = 0f,
                center = center
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSpherePath(
    center: Offset,
    radius: Float,
    verticalSquash: Float,
    rotationDeg: Float,
    color: androidx.compose.ui.graphics.Color
) {
    val steps = 60
    var prev: Offset? = null

    for (i in 0..steps) {
        val angle = Math.toRadians(
            (i * (360.0 / steps)) + rotationDeg
        )

        val x = center.x +
                radius * cos(angle).toFloat()

        val y = center.y +
                radius *
                sin(angle).toFloat() *
                verticalSquash

        val point = Offset(x, y)

        if (prev != null) {
            drawLine(
                color = color,
                start = prev,
                end = point,
                strokeWidth = 1.2f
            )
        }

        prev = point
    }
}
