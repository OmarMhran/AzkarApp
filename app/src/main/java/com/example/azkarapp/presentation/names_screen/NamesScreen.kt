package com.example.azkarapp.presentation.names_screen

import com.example.azkarapp.presentation.common.ErrorRetry
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.azkarapp.domain.model.Names
import com.example.azkarapp.util.ShareUtils

@Composable
private fun namesAccent(): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) Color(0xFF6BC5D2) else Color(0xFF1F7A8C)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamesScreen(
    viewModel: NamesViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val accent = namesAccent()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp, 20.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(accent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "أسماء الله الحسنى",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onAction(NamesAction.SearchChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    placeholder = { Text("ابحث في أسماء الله الحسنى أو المعنى...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "بحث", tint = accent)
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onAction(NamesAction.SearchChanged("")) }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "مسح", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = accent.copy(alpha = 0.35f),
                        focusedContainerColor = accent.copy(alpha = 0.08f),
                        unfocusedContainerColor = accent.copy(alpha = 0.08f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = accent
                        )
                    } else if (state.error != null) {
                        ErrorRetry(
                            message = state.error ?: "حدث خطأ ما",
                            onRetry = { viewModel.onAction(NamesAction.Retry) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (state.names.isEmpty()) {
                        Text(
                            text = "لا توجد نتائج مطابقة لبحثك",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(
                                items = state.names,
                                key = { _, name -> name.id },
                                contentType = { _, _ -> "name" }
                            ) { index, name ->
                                NameItemCard(index = index + 1, title = name.name, meaning = name.meaning, onClick = { viewModel.onAction(NamesAction.SelectName(name)) })
                            }
                        }
                    }
                }
            }
        }

        state.selectedName?.let { selected ->
            NameDetailsDialog(
                nameItem = selected,
                onDismiss = { viewModel.onAction(NamesAction.DismissName) }
            )
        }
    }
}

@Composable
fun NameItemCard(
    index: Int,
    title: String,
    meaning: String,
    onClick: () -> Unit
) {
    val accent = namesAccent()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val formattedIndex = remember(index) {
        if (index < 10) "0$index" else "$index"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = if (isDark) 0.16f else 0.10f).compositeOver(MaterialTheme.colorScheme.surface)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formattedIndex,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = meaning,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun NameDetailsDialog(
    nameItem: Names,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val accent = namesAccent()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val shareText = "${nameItem.name}\n\nالمعنى: ${nameItem.meaning}"

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        ShareUtils.shareText(context, shareText)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = accent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "مشاركة",
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "إغلاق",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        containerColor = accent.copy(alpha = if (isDark) 0.16f else 0.10f).compositeOver(MaterialTheme.colorScheme.surfaceContainerHigh),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = nameItem.name,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = accent,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(color = accent.copy(alpha = 0.35f))

                Text(
                    text = "المعنى والشرح:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )

                Text(
                    text = nameItem.meaning,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Start
                )
            }
        }
    )
}