package com.example

import android.app.Application
import com.example.data.local.LinklyDatabase
import com.example.data.repository.LinkRepository
import com.example.ui.theme.ThemeManager

import com.example.utils.AppSettingsManager

class LinklyApplication : Application() {
    val database by lazy { LinklyDatabase.getDatabase(this) }
    val repository by lazy { 
        LinkRepository(database.linkDao(), database.categoryDao(), database.tagDao()) 
    }
    val themeManager by lazy { ThemeManager(this) }
    val appSettings by lazy { AppSettingsManager(this) }
}
