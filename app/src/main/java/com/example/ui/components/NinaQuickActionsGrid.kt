package com.example.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionType
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class QuickActionItem(
    val id: String,
    val titleUrdu: String,
    val titleEn: String,
    val icon: ImageVector,
    val accentColor: Color,
    val actionType: ActionType?,
    val onClick: () -> Unit
)

@Composable
fun NinaQuickActionsGrid(
    onActionClick: (ActionType) -> Unit,
    onOpenYouTubeDialog: () -> Unit,
    onOpenMapsDialog: () -> Unit,
    onOpenDialerDialog: () -> Unit,
    onOpenAvatarPicker: () -> Unit,
    onOpenVisionDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        QuickActionItem(
            id = "action_youtube",
            titleUrdu = "یوٹیوب سرچ",
            titleEn = "YouTube",
            icon = Icons.Default.SmartDisplay,
            accentColor = Color(0xFFFF0033),
            actionType = ActionType.YOUTUBE,
            onClick = onOpenYouTubeDialog
        ),
        QuickActionItem(
            id = "action_maps",
            titleUrdu = "گوگل میپس",
            titleEn = "Maps",
            icon = Icons.Default.Navigation,
            accentColor = NeonCyan,
            actionType = ActionType.MAPS,
            onClick = onOpenMapsDialog
        ),
        QuickActionItem(
            id = "action_dialer",
            titleUrdu = "کال ڈائلر",
            titleEn = "Dialer",
            icon = Icons.Default.Dialpad,
            accentColor = NeonGreen,
            actionType = ActionType.DIALER,
            onClick = onOpenDialerDialog
        ),
        QuickActionItem(
            id = "action_whatsapp",
            titleUrdu = "واٹس ایپ",
            titleEn = "WhatsApp",
            icon = Icons.Default.Chat,
            accentColor = Color(0xFF25D366),
            actionType = ActionType.WHATSAPP,
            onClick = { onActionClick(ActionType.WHATSAPP) }
        ),
        QuickActionItem(
            id = "action_camera",
            titleUrdu = "کیمرہ",
            titleEn = "Camera",
            icon = Icons.Default.CameraAlt,
            accentColor = NeonOrange,
            actionType = ActionType.CAMERA,
            onClick = { onActionClick(ActionType.CAMERA) }
        ),
        QuickActionItem(
            id = "action_languages",
            titleUrdu = "10 زبانیں",
            titleEn = "Languages",
            icon = Icons.Default.Translate,
            accentColor = NeonPurple,
            actionType = ActionType.LANGUAGES_DEMO,
            onClick = { onActionClick(ActionType.LANGUAGES_DEMO) }
        ),
        QuickActionItem(
            id = "action_vision",
            titleUrdu = "اسکرین ویژن",
            titleEn = "Vision AI",
            icon = Icons.Default.Visibility,
            accentColor = NeonCyan,
            actionType = null,
            onClick = onOpenVisionDialog
        ),
        QuickActionItem(
            id = "action_avatar",
            titleUrdu = "اوتار بدلو",
            titleEn = "Avatars",
            icon = Icons.Default.Face,
            accentColor = NeonPink,
            actionType = ActionType.AVATAR_SWITCH,
            onClick = onOpenAvatarPicker
        ),
        QuickActionItem(
            id = "action_torch",
            titleUrdu = "فلیش لائٹ",
            titleEn = "Flashlight",
            icon = Icons.Default.FlashOn,
            accentColor = Color(0xFFFFD600),
            actionType = ActionType.FLASHLIGHT,
            onClick = { onActionClick(ActionType.FLASHLIGHT) }
        )
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "⚡ فوری کمانڈز (Quick Actions)",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ویڈیو فیچرز",
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { item ->
                QuickActionCard(item = item)
            }
        }
    }
}

@Composable
fun QuickActionCard(item: QuickActionItem) {
    Surface(
        modifier = Modifier
            .width(96.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
            .clickable { item.onClick() }
            .testTag(item.id),
        color = DarkSurfaceVariant.copy(alpha = 0.85f),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(item.accentColor.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, item.accentColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.titleEn,
                    tint = item.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.titleUrdu,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = item.titleEn,
                color = TextSecondary,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
