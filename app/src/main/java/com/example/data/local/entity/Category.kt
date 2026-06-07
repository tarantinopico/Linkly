package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String,
    val iconName: String,
    val sortOrder: Int,
    @ColumnInfo(defaultValue = "1") val isAutoTaggingEnabled: Boolean = true
)
