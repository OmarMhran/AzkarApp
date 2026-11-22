package com.example.azkarapp.presentation.permission_screen

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.azkarapp.domain.model.Country
import com.example.azkarapp.ui.navigation.Screen

@Composable
fun PermissionScreen(
    navController: NavHostController,
    viewModel: PermissionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedCountry by remember {
        mutableStateOf(Country("EG", "مصر", "🇪🇬", 5, 30.0444, 31.2357, "القاهرة", "Africa/Cairo"))
    }
    var isCountryMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.suggestedCountry) {
        state.suggestedCountry?.let { selectedCountry = it }
    }

    val permissionsToRequest = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun navigateToHome() {
        viewModel.onAction(
            PermissionAction.SaveCountry(selectedCountry) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Permission.route) {
                        inclusive = true
                    }
                }
            }
        )
    }


    fun skipAndNavigate() {
        navigateToHome()
    }

    val batteryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        navigateToHome()
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            navigateToHome()
        } else {
            try {
                batteryLauncher.launch(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } catch (e: Exception) {
                navigateToHome()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Column {
            Text(
                text = "مرحباً بك",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "الرجاء إعداد تفضيلات تطبيقك ليعمل بأفضل دقة",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCountryMenuExpanded = true }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "اختر بلد إقامتك أولاً",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = selectedCountry.flag, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedCountry.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "اختر الدولة",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = isCountryMenuExpanded,
                    onDismissRequest = { isCountryMenuExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    state.countries.forEach { country ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = country.flag, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = country.name,
                                        fontSize = 14.sp,
                                        fontWeight = if (country.code == selectedCountry.code) FontWeight.Bold else FontWeight.Normal,
                                        color = if (country.code == selectedCountry.code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            onClick = {
                                selectedCountry = country
                                isCountryMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "أذونات الموقع، الإشعارات وتوفير الطاقة",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "• إذن الموقع: لتحسين دقة اتجاه القبلة ومواقيت الصلاة.\n• إذن الإشعارات: لتنبيهك بوقت الأذان بانتظام.\n• توفير الطاقة: لضمان عدم تأخير أو إيقاف التنبيهات في الخلفية.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    permissionsLauncher.launch(permissionsToRequest.toTypedArray())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "السماح بالأذونات والانتقال",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { skipAndNavigate() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = "تخطي الآن",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}