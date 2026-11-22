package com.example.azkarapp.presentation.hafz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.domain.model.HafzProgress
import com.example.azkarapp.domain.model.MushafPage
import com.example.azkarapp.domain.usecase.hafz.ObserveHafzProgressUseCase
import com.example.azkarapp.presentation.quran.arabicIndic
import com.example.azkarapp.ui.theme.goldPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HafzHomeViewModel @Inject constructor(
    observeProgress: ObserveHafzProgressUseCase
) : ViewModel() {
    val progress: StateFlow<HafzProgress> = observeProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HafzProgress())
}

/** كرت الهوم لحفظ القرآن: بيعرض التقدّم الكلي وبيفتح صفحة الحفظ. */
@Composable
fun HafzHomeCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HafzHomeViewModel = hiltViewModel()
) {
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val accent = hafzAccent()
    val p = goldPalette()
    val count = progress.memorizedCount

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = hafzTintAlpha()).compositeOver(p.card)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "حفظ القرآن والتسميع",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Text(
                    text = "${arabicIndic(count)} / ${arabicIndic(MushafPage.TOTAL_PAGES)} صفحة",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress.fraction },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = accent,
                trackColor = accent.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (count == 0) "تابع محفوظك وسمّع بصوتك" else "كمّل من حيث وقفت وسمّع صفحتك",
                fontSize = 12.sp,
                color = p.muted
            )
        }
    }
}