package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NinaInputBar(
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onToggleAutoListening: () -> Unit,
    isListening: Boolean,
    isAutoListening: Boolean,
    onSelectSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val voicePresets = listOf(
        "ہیلو نینا کیسی ہو",
        "یوٹیوب پر Neon Labs تلاش کرو",
        "گوگل میپس پر ممبئی کا راستہ دکھاؤ",
        "کال ڈائلر میں 9212345678 لگاؤ",
        "10 الگ الگ زبانوں میں بات کرو",
        "اپنا اوتار تبدیل کرو",
        "کیمرہ کھول دو",
        "فلیش لائٹ آن کرو"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.22f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Continuous Auto-listening status bar & Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (isAutoListening) NeonGreen else DarkSurfaceBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onToggleAutoListening() }
                    .testTag("auto_listen_toggle_pill"),
                color = if (isAutoListening) NeonGreen.copy(alpha = 0.15f) else DarkSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                if (isAutoListening) NeonGreen else TextSecondary,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isAutoListening) "⚡ خودکار سننے کا موڈ: آن ہے" else "🎙️ مائیک آن کریں (کلک کریں)",
                        color = if (isAutoListening) NeonGreen else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isListening) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Listening",
                        tint = NeonOrange,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "سن رہی ہوں...",
                        color = NeonOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Direct Voice Simulation Row (One-touch speech tester for emulator & mobile)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(voicePresets) { phrase ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (phrase == "ہیلو نینا کیسی ہو") NeonPink else DarkSurfaceBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectSuggestion(phrase) }
                        .testTag("voice_preset_$phrase"),
                    color = if (phrase == "ہیلو نینا کیسی ہو") NeonPink.copy(alpha = 0.15f) else DarkSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = if (phrase == "ہیلو نینا کیسی ہو") NeonPink else NeonCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = phrase,
                            color = if (phrase == "ہیلو نینا کیسی ہو") NeonPink else NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Main Input Bar Row
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            color = DarkSurface,
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                when {
                    isListening -> NeonOrange
                    isAutoListening -> NeonGreen.copy(alpha = 0.8f)
                    else -> DarkSurfaceBorder
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            when {
                                isListening -> NeonOrange
                                isAutoListening -> NeonGreen.copy(alpha = 0.25f)
                                else -> NeonCyan.copy(alpha = 0.15f)
                            }
                        )
                        .border(
                            1.5.dp,
                            if (isAutoListening) NeonGreen else Color.Transparent,
                            CircleShape
                        )
                        .clickable { onToggleAutoListening() }
                        .testTag("voice_input_mic_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else if (isAutoListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Voice Input",
                        tint = when {
                            isListening -> Color.White
                            isAutoListening -> NeonGreen
                            else -> NeonCyan
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Text Input
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChanged,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("message_input_field"),
                    placeholder = {
                        Text(
                            text = when {
                                isListening -> "سن رہی ہوں... بولیے"
                                isAutoListening -> "آٹو مائیک فعال ہے... بولیے"
                                else -> "یہاں لکھیں یا اوپر بولنے کے لیے کلک کریں..."
                            },
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonCyan
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSendMessage() })
                )

                // Send Button
                IconButton(
                    onClick = onSendMessage,
                    modifier = Modifier
                        .size(40.dp)
                        .background(NeonPink, CircleShape)
                        .testTag("send_message_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
