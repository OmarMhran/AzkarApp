package com.example.azkarapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.azkarapp.data.local.database.entity.CategoryEntity
import com.example.azkarapp.data.local.database.entity.CategorySupplicationCrossRef

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategorySupplicationCrossRefs(refs: List<CategorySupplicationCrossRef>)

    @Query("SELECT * FROM categories WHERE groupId = :groupId ORDER BY weight ASC")
    suspend fun getCategoriesByGroup(groupId: Int): List<CategoryEntity>
}