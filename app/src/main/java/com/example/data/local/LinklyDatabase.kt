package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.LinkDao
import com.example.data.local.dao.TagDao
import com.example.data.local.dao.AutoTagRuleDao
import com.example.data.local.entity.Category
import com.example.data.local.entity.Link
import com.example.data.local.entity.LinkTagCrossRef
import com.example.data.local.entity.Tag
import com.example.data.local.entity.AutoTagRule

@Database(
    entities = [
        Link::class,
        Category::class,
        Tag::class,
        LinkTagCrossRef::class,
        AutoTagRule::class
    ],
    version = 5,
    exportSchema = false
)
abstract class LinklyDatabase : RoomDatabase() {
    abstract fun linkDao(): LinkDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao
    abstract fun autoTagRuleDao(): AutoTagRuleDao

    companion object {
        @Volatile
        private var INSTANCE: LinklyDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN isAutoTaggingEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("CREATE TABLE IF NOT EXISTS auto_tag_rules (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `domain` TEXT NOT NULL, `tagName` TEXT NOT NULL)")
                
                // Přidání výchozích pravidel
                db.execSQL("INSERT INTO auto_tag_rules (domain, tagName) VALUES ('youtube.com', 'Video')")
                db.execSQL("INSERT INTO auto_tag_rules (domain, tagName) VALUES ('youtu.be', 'Video')")
                db.execSQL("INSERT INTO auto_tag_rules (domain, tagName) VALUES ('github.com', 'Kód')")
                db.execSQL("INSERT INTO auto_tag_rules (domain, tagName) VALUES ('medium.com', 'Článek')")
                db.execSQL("INSERT INTO auto_tag_rules (domain, tagName) VALUES ('spotify.com', 'Hudba')")
            }
        }

        fun getDatabase(context: Context): LinklyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LinklyDatabase::class.java,
                    "linkly_database"
                )
                .addMigrations(MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
