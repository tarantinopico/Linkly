package com.example.utils

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _useInternalBrowser = MutableStateFlow(prefs.getBoolean("use_internal_browser", true))
    val useInternalBrowser: StateFlow<Boolean> = _useInternalBrowser.asStateFlow()

    fun setUseInternalBrowser(useInternal: Boolean) {
        prefs.edit().putBoolean("use_internal_browser", useInternal).apply()
        _useInternalBrowser.value = useInternal
    }

    private val _isFirstLaunch = MutableStateFlow(prefs.getBoolean("is_first_launch", true))
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()

    fun setFirstLaunchCompleted() {
        prefs.edit().putBoolean("is_first_launch", false).apply()
        _isFirstLaunch.value = false
    }

    private val _lastSortOrder = MutableStateFlow(prefs.getString("last_sort_order", "NEWEST") ?: "NEWEST")
    val lastSortOrder: StateFlow<String> = _lastSortOrder.asStateFlow()

    fun setLastSortOrder(sortOrder: String) {
        prefs.edit().putString("last_sort_order", sortOrder).apply()
        _lastSortOrder.value = sortOrder
    }
}
