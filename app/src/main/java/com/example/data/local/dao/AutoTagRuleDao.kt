package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.AutoTagRule
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoTagRuleDao {
    @Query("SELECT * FROM auto_tag_rules")
    fun getAllRules(): Flow<List<AutoTagRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AutoTagRule)

    @Delete
    suspend fun deleteRule(rule: AutoTagRule)
}
