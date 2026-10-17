
package com.example.azkarapp.presentation.main_hub.home_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.azkarapp.data.local.AzkarProgress
import kotlinx.coroutines.delay


data class AzkarProgress(
    val categoryId: Int,
    val categoryName: String,
    val completed: Int,
    val total: Int
)

@Composable
fun AzkarProgressBadge(
    progress: AzkarProgress,
    onBadgeClick: () -> Unit
) {
    var showProgressTip by remember(progress.categoryId) { mutableStateOf(true) }

    LaunchedEffect(progress.categoryId) {
        showProgressTip = true
        delay(4000)
        showProgressTip = false
    }

    val percentage = if (progress.total > 0) {
        ((progress.completed.toFloat() / progress.total) * 100).toInt()
    } else 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = showProgressTip,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = "متابعة: ${progress.categoryName}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
            modifier = Modifier
                .size(42.dp)
                .clickable(onClick = onBadgeClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$percentage%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}