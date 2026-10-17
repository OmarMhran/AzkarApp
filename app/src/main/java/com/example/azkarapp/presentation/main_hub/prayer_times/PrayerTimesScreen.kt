package com.example.azkarapp.presentation.main_hub.prayer_times

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.azkarapp.util.openAppSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PrayerTimesScreen(
    viewModel: PrayerTimesViewModel = hiltViewModel(),
    onNavigateToCompass: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val showShimmer = uiState.isLoading && uiState.prayersList.isEmpty()
    var shimmerVisible by remember { mutableStateOf(false) }
    LaunchedEffect(showShimmer) {
        if (showShimmer) {
            delay(300)
            shimmerVisible = true
        } else {
            shimmerVisible = false
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onAction(PrayerTimesAction.Refresh)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val openSettings = remember(context) { { context.openAppSettings() } }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isPermissionDenied -> {
                MissingPermissionsContent(onOpenSettings = openSettings)
            }

            uiState.errorMessage != null && uiState.prayersList.isEmpty() -> {
                NetworkErrorContent(
                    message = uiState.errorMessage!!,
                    onRetry = { viewModel.onAction(PrayerTimesAction.Refresh) }
                )
            }

            uiState.isLoading && uiState.prayersList.isEmpty() -> {
                if (shimmerVisible) PrayerTimesShimmerLoading() else Box(Modifier.fillMaxSize())
            }

            else -> {
                PrayerTimesMainContent(
                    uiState = uiState,
                    countdownTimeFlow = viewModel.countdownTime,
                    onNavigateToCompass = onNavigateToCompass,
                    onToggleNotification = { keyName ->
                        viewModel.onAction(PrayerTimesAction.ToggleNotification(keyName))
                    },
                    onOpenSettings = openSettings
                )
            }
        }
    }
}

@Composable
private fun PrayerTimesMainContent(
    uiState: PrayerTimesUiState,
    countdownTimeFlow: StateFlow<String>,
    onNavigateToCompass: () -> Unit,
    onToggleNotification: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 24.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مواقيت الصلاة",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = { onNavigateToCompass() },
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "الاتجاه للقبلة",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        val missingLocation = !uiState.hasLocationPermission
        val missingNotification = !uiState.hasNotificationPermission

        if (missingLocation || missingNotification) {
            val alertTitle: String
            val alertMessage: String

            when {
                missingLocation && missingNotification -> {
                    alertTitle = "الموقع والإشعارات غير مفعلة"
                    alertMessage = "يرجى السماح بصلاحيات الموقع لضمان دقة أوقات الصلاة، والإشعارات لتنبيهك بالصلوات."
                }
                missingLocation -> {
                    alertTitle = "المواقيت قد تكون غير دقيقة"
                    alertMessage = "لم تقم بالسماح بصلاحيات الموقع، مما يؤثر على دقة حساب أوقات الصلاة."
                }
                else -> {
                    alertTitle = "الإشعارات معطلة"
                    alertMessage = "لن يتم إرسال تنبيهات الأذان لعدم السماح بصلاحيات الإشعارات."
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alertTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = alertMessage,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onOpenSettings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(text = "تفعيل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = uiState.cityName,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "المتبقي لصلاة ${uiState.nextPrayerName}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    CountdownText(countdownTimeFlow)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = uiState.hijriDate,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = uiState.prayersList,
                key = { prayer -> prayer.realKeyName },
                contentType = { _ -> "PrayerCard" }
            ) { prayer ->
                NewPrayerCard(
                    prayer = prayer,
                    onToggleNotification = onToggleNotification
                )
            }
        }
    }
}

@Composable
private fun CountdownText(countdownTimeFlow: StateFlow<String>) {
    val countdownTime by countdownTimeFlow.collectAsStateWithLifecycle()
    Text(
        text = countdownTime,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 36.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp
    )
}
