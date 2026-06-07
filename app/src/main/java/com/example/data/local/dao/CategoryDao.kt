package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

data class CategoryWithCount(
    @androidx.room.Embedded val category: Category,
    @androidx.room.ColumnInfo(name = "linkCount") val linkCount: Int
)

@Dao
interface CategoryDao {
    @Query("SELECT c.*, (SELECT COUNT(id) FROM links WHERE categoryId = c.id) as linkCount FROM categories c ORDER BY c.sortOrder ASC")
    fun getCategoriesWithCount(): Flow<List<CategoryWithCount>>
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesList(): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)
}
