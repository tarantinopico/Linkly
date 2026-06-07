package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.LinkDao
import com.example.data.local.dao.TagDao
import com.example.data.local.entity.Category
import com.example.data.local.entity.Link
import com.example.data.local.entity.LinkTagCrossRef
import com.example.data.local.entity.Tag

@Database(
    entities = [
        Link::class,
        Category::class,
        Tag::class,
        LinkTagCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class LinklyDatabase : RoomDatabase() {
    abstract fun linkDao(): LinkDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: LinklyDatabase? = null

        fun getDatabase(context: Context): LinklyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LinklyDatabase::class.java,
                    "linkly_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
