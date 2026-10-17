package com.example.azkarapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.azkarapp.data.local.database.entity.SupplicationEntity

@Dao
interface SupplicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplications(supplications: List<SupplicationEntity>)

    @Query("""
        SELECT s.* FROM supplications s 
        INNER JOIN category_supplication_cross_ref ref ON s.id = ref.supplicationId 
        WHERE ref.categoryId = :categoryId
    """)
    suspend fun getSupplicationsByCategory(categoryId: Int): List<SupplicationEntity>
}
