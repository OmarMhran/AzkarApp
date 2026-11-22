package com.example.azkarapp.data.local.quran

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val number: Int,
    val name: String,
    val ayahCount: Int,
    val bismillah: String,
    val isMedinan: Boolean,
    val startPage: Int
)

@Entity(tableName = "ayahs", primaryKeys = ["surahNumber", "ayahNumber"])
data class AyahEntity(
    val surahNumber: Int,
    val ayahNumber: Int,
    /** النص العثماني زي ما هو في المصدر بدون أي تعديل. */
    val text: String,
    /** نسخة مبسّطة للبحث فقط. */
    val textSimple: String,
    /** نسخة بديلة للبحث: الألف الخنجرية متحولة لألف عادية. */
    val textAlt: String,
    val page: Int,
    val juz: Int
)

@Entity(tableName = "bookmarks", primaryKeys = ["surahNumber", "ayahNumber"])
data class BookmarkEntity(
    val surahNumber: Int,
    val ayahNumber: Int,
    val createdAt: Long
)

@Entity(tableName = "last_read")
data class LastReadEntity(
    @PrimaryKey val id: Int = 1,
    val page: Int,
    val surahNumber: Int,
    val ayahNumber: Int
)

data class AyahKeyRow(
    val surahNumber: Int,
    val ayahNumber: Int
)

data class AyahMatchRow(
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val text: String,
    val page: Int
)

data class LastReadRow(
    val page: Int,
    val surahName: String,
    val ayahNumber: Int
)
