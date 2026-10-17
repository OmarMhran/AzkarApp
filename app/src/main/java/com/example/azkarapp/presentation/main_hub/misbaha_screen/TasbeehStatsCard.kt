package com.example.azkarapp.presentation.main_hub.misbaha_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azkarapp.data.local.database.dao.PeriodStat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private const val HEATMAP_WEEKS = 12
private const val BAR_DAYS = 7

private fun shortDayName(day: DayOfWeek): String = when (day) {
    DayOfWeek.SATURDAY -> "سبت"
    DayOfWeek.SUNDAY -> "أحد"
    DayOfWeek.MONDAY -> "اثنين"
    DayOfWeek.TUESDAY -> "ثلاثاء"
    DayOfWeek.WEDNESDAY -> "أربعاء"
    DayOfWeek.THURSDAY -> "خميس"
    DayOfWeek.FRIDAY -> "جمعة"
}

private fun compact(value: Int): String =
    if (value >= 1000) String.format(Locale.US, "%.1fk", value / 1000.0) else value.toString()


@Composable
fun TasbeehStatsCard(dailyStats: List<PeriodStat>) {
    val primary = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceContainerHigh

    // بيتحدّث لوحده لو التطبيق فضل مفتوح بعد نص الليل
    val today by produceState(LocalDate.now()) {
        while (true) {
            delay(60_000)
            value = LocalDate.now()
        }
    }
    val data: Map<LocalDate, Int> = remember(dailyStats) {
        dailyStats.mapNotNull { s ->
            runCatching { LocalDate.parse(s.period) to s.totalCount }.getOrNull()
        }.toMap()
    }

    val todayCount = data[today] ?: 0
    val last7 = (0 until BAR_DAYS).map { today.minusDays((BAR_DAYS - 1 - it).toLong()) }
    val last7Total = last7.sumOf { data[it] ?: 0 }
    val bestDay = data.values.maxOrNull() ?: 0
    val streak = remember(dailyStats, today) {
        var day = if ((data[today] ?: 0) > 0) today else today.minusDays(1)
        var n = 0
        while ((data[day] ?: 0) > 0) { n++; day = day.minusDays(1) }
        n
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "نشاطك في التسبيح",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = primary
            )

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد نشاطات مسجلة بعد",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                return@Column
            }

            // 1) ملخص سريع
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryTile("اليوم", compact(todayCount), Modifier.weight(1f))
                SummaryTile("آخر ٧ أيام", compact(last7Total), Modifier.weight(1f))
                SummaryTile("أفضل يوم", compact(bestDay), Modifier.weight(1f))
                SummaryTile("أيام متتالية", streak.toString(), Modifier.weight(1f))
            }

            // 2) أعمدة آخر 7 أيام
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("آخر ٧ أيام")
                val maxBar = (last7.maxOf { data[it] ?: 0 }).coerceAtLeast(1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    last7.forEach { day ->
                        val count = data[day] ?: 0
                        val isToday = day == today
                        val barHeight = (84f * count / maxBar).coerceAtLeast(if (count > 0) 6f else 3f)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = if (count > 0) compact(count) else "",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(barHeight.dp)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                                    .background(
                                        when {
                                            count == 0 -> emptyColor
                                            isToday -> primary
                                            else -> primary.copy(alpha = 0.4f)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = shortDayName(day.dayOfWeek),
                                fontSize = 10.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3) خريطة النشاط: كل عمود أسبوع، كل صف يوم
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("آخر $HEATMAP_WEEKS أسبوع")
                val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY))
                val firstWeek = weekStart.minusWeeks((HEATMAP_WEEKS - 1).toLong())
                val maxCell = data.filterKeys { !it.isBefore(firstWeek) }.values.maxOrNull()?.coerceAtLeast(1) ?: 1

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(HEATMAP_WEEKS) { w ->
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(7) { d ->
                                val date = firstWeek.plusWeeks(w.toLong()).plusDays(d.toLong())
                                val count = data[date] ?: 0
                                val future = date.isAfter(today)
                                val ratio = count.toFloat() / maxCell
                                val color = when {
                                    future -> androidx.compose.ui.graphics.Color.Transparent
                                    count == 0 -> emptyColor
                                    ratio < 0.25f -> primary.copy(alpha = 0.3f)
                                    ratio < 0.5f -> primary.copy(alpha = 0.55f)
                                    ratio < 0.75f -> primary.copy(alpha = 0.8f)
                                    else -> primary
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color)
                                        .then(
                                            if (date == today)
                                                Modifier.border(1.5.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(4.dp))
                                            else Modifier
                                        )
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("أقل", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    listOf(emptyColor, primary.copy(alpha = 0.3f), primary.copy(alpha = 0.55f),
                        primary.copy(alpha = 0.8f), primary).forEach {
                        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(it))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("أكثر", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SummaryTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}