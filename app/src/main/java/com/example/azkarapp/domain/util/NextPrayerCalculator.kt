package com.example.azkarapp.domain.util

import com.example.azkarapp.data.repo.NextPrayerInfo
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale


object NextPrayerCalculator {

    private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

    fun parseTime(text: String): LocalTime? = try {
        LocalTime.parse(text.replace("ص", "AM").replace("م", "PM"), timeFormat)
    } catch (e: DateTimeParseException) {
        null
    }

    fun calculate(
        prayerTimes: Map<String, String>,
        now: LocalDateTime = LocalDateTime.now()
    ): NextPrayerInfo {
        val valid = prayerTimes.mapNotNull { (name, text) ->
            parseTime(text)?.let { Triple(name, text, it) }
        }
        if (valid.isEmpty()) {
            return NextPrayerInfo(
                name = "--",
                time = "--",
                remainingTime = "--",
                countdownFormatted = "00:00:00",
                isNextDay = false
            )
        }

        val today = now.toLocalDate()
        val next = valid.firstOrNull { (_, _, time) -> today.atTime(time).isAfter(now) }
        val isNextDay = next == null
        val (key, text, time) = next ?: valid.first()

        val isFriday = now.dayOfWeek == DayOfWeek.FRIDAY
        val displayName = if (key == "الظهر" && isFriday) "الجمعة" else key

        val target = (if (isNextDay) today.plusDays(1) else today).atTime(time)
        val diff = Duration.between(now, target).coerceAtLeast(Duration.ZERO)

        val hours = diff.toHours()
        val minutes = diff.toMinutes() % 60
        val seconds = diff.seconds % 60

        return NextPrayerInfo(
            name = displayName,
            time = text,
            remainingTime = if (hours > 0) "خلال $hours س و $minutes ق" else "خلال $minutes دقيقة",
            countdownFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds),
            isNextDay = isNextDay
        )
    }
}
