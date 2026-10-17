package com.example.azkarapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.azkarapp.data.local.database.dao.CategoryDao
import com.example.azkarapp.data.local.database.dao.GroupDao
import com.example.azkarapp.data.local.database.dao.MisbahaDao
import com.example.azkarapp.data.local.database.dao.NamesDao
import com.example.azkarapp.data.local.database.dao.SupplicationDao
import com.example.azkarapp.data.local.database.entity.CategoryEntity
import com.example.azkarapp.data.local.database.entity.CategorySupplicationCrossRef
import com.example.azkarapp.data.local.database.entity.CustomZikrEntity
import com.example.azkarapp.data.local.database.entity.GroupEntity
import com.example.azkarapp.data.local.database.entity.NamesEntity
import com.example.azkarapp.data.local.database.entity.SupplicationEntity
import com.example.azkarapp.data.local.database.entity.TasbeehHistoryEntity


@Database(
    entities = [
        GroupEntity::class,
        CategoryEntity::class,
        SupplicationEntity::class,
        CategorySupplicationCrossRef::class,
        CustomZikrEntity::class,
        TasbeehHistoryEntity::class,
        NamesEntity::class


    ],
    version = 3,
    exportSchema = true
)
abstract class AzkarDatabase : RoomDatabase(){
    abstract fun groupDao(): GroupDao
    abstract fun categoryDao(): CategoryDao
    abstract fun supplicationDao(): SupplicationDao
    abstract fun misbahaDao(): MisbahaDao
    abstract fun namesDao(): NamesDao
}
