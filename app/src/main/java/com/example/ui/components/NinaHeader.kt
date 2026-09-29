package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NinaHeader(
    currentLanguage: AppLanguage,
    isMuted: Boolean,
    isFlashlightOn: Boolean,
    onLanguageSelected: (AppLanguage) -> Unit,
    onToggleMute: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onOpenAgentHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    var languageMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DarkSurface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: App Identity & Core Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(NeonGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NINA",
                            color = NeonCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CORE v2.5",
                            color = NeonPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "نینا پرسنل اے آئی اسسٹنٹ",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // Right: Actions (Language, Audio Mute, Torch, Agent Hub)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Language Dropdown Selector
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { languageMenuExpanded = true }
                            .testTag("language_selector_btn"),
                        color = DarkSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentLanguage.nativeName,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = languageMenuExpanded,
                        onDismissRequest = { languageMenuExpanded = false }
                    ) {
                        AppLanguage.values().forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${lang.nativeName} (${lang.name})",
                                        fontWeight = if (lang == currentLanguage) FontWeight.Bold else FontWeight.Normal,
                                        color = if (lang == currentLanguage) NeonCyan else TextPrimary
                                    )
                                },
                                onClick = {
                                    onLanguageSelected(lang)
                                    languageMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Mute / Speech Toggle
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier.size(36.dp).testTag("speech_mute_btn")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Toggle Audio",
                        tint = if (isMuted) Color.Gray else NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Flashlight Toggle
                IconButton(
                    onClick = onToggleFlashlight,
                    modifier = Modifier.size(36.dp).testTag("torch_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isFlashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Torch",
                        tint = if (isFlashlightOn) Color(0xFFFFD600) else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Agent Hub / NIM Settings
                IconButton(
                    onClick = onOpenAgentHub,
                    modifier = Modifier.size(36.dp).testTag("agent_hub_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = "Open Agent Hub",
                        tint = NeonPink,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
