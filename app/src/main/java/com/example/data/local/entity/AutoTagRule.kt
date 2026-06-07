package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auto_tag_rules")
data class AutoTagRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val domain: String,
    val tagName: String
)
