package com.example.azkarapp.data.local.quran

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * قاعدة بيانات المصحف منفصلة عن AzkarDatabase علشان ما نلمسش الـ schema بتاعها
 * ومفيش migration على بيانات الأذكار.
 */
@Database(
    entities = [
        SurahEntity::class,
        AyahEntity::class,
        BookmarkEntity::class,
        LastReadEntity::class,
        HafzPageEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
    abstract fun hafzDao(): HafzDao

    companion object {
        /** 3 -> 4: إضافة جدول الصفحات المحفوظة بدون لمس المفضلة أو آخر قراءة. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `hafz_pages` " +
                        "(`page` INTEGER NOT NULL, `memorizedAt` INTEGER NOT NULL, PRIMARY KEY(`page`))"
                )
            }
        }
    }
}
