package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssistantStatus
import com.example.model.AvatarType
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NinaAvatarView(
    avatar: AvatarType,
    status: AssistantStatus,
    currentSpokenText: String? = null,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_motion")

    // Breathing scale animation
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Rotation for neon particle rings
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    // Pulse for glow ring
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(230.dp)
                .clickable { onAvatarClick() }
                .testTag("nina_avatar_container"),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Aura Halo Ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (size.minDimension / 2f) - 10f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.35f * glowAlpha),
                            NeonPink.copy(alpha = 0.15f * glowAlpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 1.25f
                    ),
                    radius = radius * 1.15f,
                    center = center
                )

                for (i in 0 until 8) {
                    val angleRad = Math.toRadians((rotationAngle + i * 45).toDouble())
                    val markerX = center.x + (radius + 2f) * cos(angleRad).toFloat()
                    val markerY = center.y + (radius + 2f) * sin(angleRad).toFloat()
                    drawCircle(
                        color = if (i % 2 == 0) NeonCyan.copy(alpha = 0.8f) else NeonPink.copy(alpha = 0.7f),
                        radius = if (i % 2 == 0) 3.5f else 2f,
                        center = Offset(markerX, markerY)
                    )
                }

                drawCircle(
                    color = NeonCyan.copy(alpha = 0.4f),
                    radius = radius,
                    style = Stroke(width = 2f)
                )
            }

            // Main Avatar Circle
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .scale(if (status == AssistantStatus.SPEAKING || status == AssistantStatus.LISTENING) breathingScale * 1.03f else breathingScale)
                    .shadow(16.dp, CircleShape, spotColor = NeonCyan)
                    .clip(CircleShape)
                    .border(
                        width = 2.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(NeonCyan, NeonPink, NeonPurple, NeonCyan)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (avatar.drawableRes != null) {
                    Image(
                        painter = painterResource(id = avatar.drawableRes),
                        contentDescription = "Nina Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    NeonCoreVisualizer(status = status, rotation = rotationAngle)
                }
            }

            // Floating Switcher Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 4.dp),
                shape = CircleShape,
                color = DarkSurface,
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Switch Avatar",
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = avatar.titleUrdu,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Status Indicator Pill
        StatusBadge(status = status)

        // Live Spoken Subtitle Ticker (so user sees words even if browser audio is muted)
        AnimatedVisibility(
            visible = status == AssistantStatus.SPEAKING && !currentSpokenText.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceVariant.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentSpokenText ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: AssistantStatus) {
    val (statusColor, statusIcon, label) = when (status) {
        AssistantStatus.IDLE -> Triple(NeonGreen, Icons.Default.AutoAwesome, "نینا فعال ہے • تیار ہے")
        AssistantStatus.LISTENING -> Triple(NeonOrange, Icons.Default.Hearing, "سن رہی ہوں... بولیے")
        AssistantStatus.PROCESSING -> Triple(NeonPurple, Icons.Default.AutoAwesome, "سوچ رہی ہوں...")
        AssistantStatus.SPEAKING -> Triple(NeonCyan, Icons.Default.VolumeUp, "بول رہی ہوں...")
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DarkSurface.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.7f)),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(statusColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun NeonCoreVisualizer(status: AssistantStatus, rotation: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00384D), Color(0xFF050C16)),
                center = center,
                radius = maxR
            ),
            radius = maxR,
            center = center
        )

        drawCircle(
            color = NeonCyan.copy(alpha = 0.6f),
            radius = maxR * 0.75f,
            style = Stroke(width = 3f)
        )

        drawCircle(
            color = NeonPink.copy(alpha = 0.5f),
            radius = maxR * 0.5f,
            style = Stroke(width = 2f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, NeonCyan, Color.Transparent),
                center = center,
                radius = maxR * 0.35f
            ),
            radius = maxR * 0.35f,
            center = center
        )

        val numSpokes = 12
        for (i in 0 until numSpokes) {
            val angle = Math.toRadians((rotation * 2 + i * (360 / numSpokes)).toDouble())
            val startX = center.x + (maxR * 0.45f) * cos(angle).toFloat()
            val startY = center.y + (maxR * 0.45f) * sin(angle).toFloat()
            val endX = center.x + (maxR * 0.72f) * cos(angle).toFloat()
            val endY = center.y + (maxR * 0.72f) * sin(angle).toFloat()

            drawLine(
                color = if (i % 2 == 0) NeonCyan.copy(alpha = 0.7f) else NeonPink.copy(alpha = 0.6f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.5f
            )
        }
    }
}
