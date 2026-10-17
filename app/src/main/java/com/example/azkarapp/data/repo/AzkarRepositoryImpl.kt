package com.example.azkarapp.data.repo

import android.util.Log
import androidx.room.withTransaction
import com.example.azkarapp.data.local.assets.JsonAssetReader
import com.example.azkarapp.data.local.database.AzkarDatabase
import com.example.azkarapp.data.local.database.dao.CategoryDao
import com.example.azkarapp.data.local.database.dao.GroupDao
import com.example.azkarapp.data.local.database.dao.NamesDao
import com.example.azkarapp.data.local.database.dao.SupplicationDao
import com.example.azkarapp.data.local.database.entity.CategoryEntity
import com.example.azkarapp.data.local.database.entity.CategorySupplicationCrossRef
import com.example.azkarapp.data.local.database.entity.GroupEntity
import com.example.azkarapp.data.local.database.entity.NamesEntity
import com.example.azkarapp.data.local.database.entity.SupplicationEntity
import com.example.azkarapp.domain.model.AzkarCategory
import com.example.azkarapp.domain.model.AzkarGroup
import com.example.azkarapp.domain.model.AzkarSupplication
import com.example.azkarapp.domain.model.Names
import com.example.azkarapp.domain.repository.AzkarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AzkarRepositoryImpl @Inject constructor(
    private val database: AzkarDatabase,
    private val assetReader: JsonAssetReader,
    private val groupDao: GroupDao,
    private val categoryDao: CategoryDao,
    private val supplicationDao: SupplicationDao,
    private val namesDao: NamesDao
) : AzkarRepository {

    private val seedMutex = Mutex()
    @Volatile private var seeded = false

    override suspend fun populateDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        if (seeded) return@withContext

        seedMutex.withLock {
            if (seeded) return@withLock

            // مع الـ transaction تحت، يا كل الجداول اتكتبت يا ولا واحد، فالفحص ده كفاية
            if (groupDao.getAllGroups().isNotEmpty()) {
                seeded = true
                return@withLock
            }

            val groupsDto = assetReader.readGroups()
            val categoriesDto = assetReader.readCategories()
            val supplicationsDto = assetReader.readSupplications()
            val namesDto = assetReader.readNames()

            if (groupsDto.isEmpty() || supplicationsDto.isEmpty()) {
                Log.e(TAG, "assets empty or unreadable, seeding skipped")
                return@withLock
            }

            val supplicationEntities = supplicationsDto.map { dto ->
                SupplicationEntity(
                    id = dto.id,
                    repeat = dto.repeat,
                    body = dto.body,
                    bodyVocalized = dto.bodyVocalized,
                    note = dto.note
                )
            }

            val groupEntities = mutableListOf<GroupEntity>()
            val categoryEntities = mutableListOf<CategoryEntity>()
            val crossReferences = mutableListOf<CategorySupplicationCrossRef>()
            val categoryDtoMap = categoriesDto.associateBy { it.id }

            groupsDto.forEach { groupDto ->
                groupEntities.add(
                    GroupEntity(id = groupDto.id, name = groupDto.name, icon = groupDto.icon)
                )

                groupDto.categories.forEach { wrapper ->
                    val categoryDto = categoryDtoMap[wrapper.id]
                    if (categoryDto != null) {
                        categoryEntities.add(
                            CategoryEntity(
                                id = categoryDto.id,
                                groupId = groupDto.id,
                                weight = categoryDto.weight,
                                name = categoryDto.name,
                                nameSearch = categoryDto.nameSearch,
                                favorite = if (categoryDto.favorite) 1 else 0
                            )
                        )
                        categoryDto.supplications.forEach { suppWrapper ->
                            crossReferences.add(
                                CategorySupplicationCrossRef(
                                    categoryId = categoryDto.id,
                                    supplicationId = suppWrapper.id
                                )
                            )
                        }
                    }
                }
            }

            val namesEntities = namesDto.map { dto ->
                NamesEntity(id = dto.id, name = dto.name, meaning = dto.meaning)
            }

            database.withTransaction {
                supplicationDao.insertSupplications(supplicationEntities)
                groupDao.insertGroups(groupEntities)
                categoryDao.insertCategories(categoryEntities)
                categoryDao.insertCategorySupplicationCrossRefs(crossReferences)
                namesDao.insertNames(namesEntities)
            }
            seeded = true
        }
    }

    override suspend fun getGroups(): List<AzkarGroup> = withContext(Dispatchers.IO) {
        populateDatabaseIfNeeded()
        groupDao.getAllGroups().map { it.toDomainGroup() }
    }

    override suspend fun getCategoriesByGroup(groupId: Int): List<AzkarCategory> = withContext(Dispatchers.IO) {
        populateDatabaseIfNeeded()
        categoryDao.getCategoriesByGroup(groupId).map { it.toDomainCategory() }
    }

    override suspend fun getSupplicationsByCategory(categoryId: Int): List<AzkarSupplication> = withContext(Dispatchers.IO) {
        populateDatabaseIfNeeded()
        supplicationDao.getSupplicationsByCategory(categoryId).map { it.toDomainSupplication() }
    }

    override suspend fun getNames(): List<Names> = withContext(Dispatchers.IO) {
        populateDatabaseIfNeeded()
        namesDao.getAllNames().map { it.toDomainNames() }
    }

    private fun NamesEntity.toDomainNames() = Names(id = id, name = name, meaning = meaning)

    private fun GroupEntity.toDomainGroup() = AzkarGroup(id = id, name = name, icon = icon)

    private fun CategoryEntity.toDomainCategory() = AzkarCategory(
        id = id,
        groupId = groupId,
        weight = weight,
        name = name,
        nameSearch = nameSearch,
        isFavorite = favorite == 1
    )

    private fun SupplicationEntity.toDomainSupplication() = AzkarSupplication(
        id = id,
        repeat = repeat,
        body = body,
        bodyVocalized = bodyVocalized,
        note = note
    )

    private companion object {
        const val TAG = "AzkarRepository"
    }
}