package com.example.azkarapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.azkarapp.data.local.database.entity.NamesEntity

@Dao
interface NamesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNames(namse: List<NamesEntity>)

    @Query("SELECT * FROM names")
    suspend fun getAllNames(): List<NamesEntity>

    @Query("SELECT * FROM names WHERE id = :nameId")
    suspend fun getNamesById(nameId: Int): List<NamesEntity>
}