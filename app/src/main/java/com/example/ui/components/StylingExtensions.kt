package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GymOrange

// Color constants for our advanced shaders
val DarkShaderStart = Color(0xFF0F172A)
val DarkShaderEnd = Color(0xFF020617)
val GlowNeonLime = Color(0xFFA3E635)
val GlowNeonCyan = Color(0xFF06B6D4)
val GlowNeonPurple = Color(0xFF8B5CF6)
val GymMutedCustomHex = Color(0xFF475569)

/**
 * Animated Gradient Shader background that moves smoothly over time.
 * Replaces plain flat colors with an elegant high-fidelity athletic atmosphere.
 */
fun Modifier.glowShader(
    colors: List<Color> = listOf(DarkShaderStart, GlowNeonLilac(), DarkShaderEnd, GlowNeonDeepLime()),
    durationMillis: Int = 4000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_shader")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    this.drawBehind {
        val width = size.width
        val height = size.height
        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(animatedOffset - 1000f, animatedOffset - 1000f),
            end = Offset(animatedOffset + width, animatedOffset + height),
            tileMode = TileMode.Mirror
        )
        drawRect(brush = brush)
    }
}

/**
 * Glowing colorful border shader that moves along the element border.
 */
fun Modifier.borderShader(
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.5.dp,
    durationMillis: Int = 3000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "border_shader")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    val listColors = listOf(
        GlowNeonLime,
        GlowNeonCyan,
        GlowNeonPurple,
        GlowNeonLime
    )

    this.border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listColors,
            start = Offset(animatedOffset, 0f),
            end = Offset(animatedOffset + 500f, 500f)
        ),
        shape = shape
    )
}

/**
 * Animated spring scale selected state.
 * Smoothly scales up selected units and shrinks unselected units with visual weight.
 */
fun Modifier.scaleSelected(
    isSelected: Boolean,
    selectedScale: Float = 1.04f,
    unselectedScale: Float = 0.98f
): Modifier = composed {
    val targetScale = if (isSelected) selectedScale else unselectedScale
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale_anim"
    )
    this.scale(scale)
}

/**
 * Tap Bounce interactive spring modifier.
 * Bounces on click and provides haptic feedback style responsiveness.
 */
@Suppress("DEPRECATION")
fun Modifier.bounceClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "bounce"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.foundation.LocalIndication.current,
            enabled = enabled,
            onClick = onClick
        )
}

// Helper colors for dynamic look
private fun GlowNeonLilac() = Color(0xFF6366F1)
private fun GlowNeonDeepLime() = Color(0xFF76A81D)
