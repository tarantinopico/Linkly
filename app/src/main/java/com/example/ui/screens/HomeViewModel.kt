package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Category
import com.example.data.local.dao.CategoryWithCount
import com.example.data.local.entity.Link
import com.example.data.local.entity.LinkWithTagsAndCategory
import com.example.data.repository.LinkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.example.utils.AppSettingsManager
import org.jsoup.Jsoup
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class SortOrder(val displayName: String) { 
    NEWEST("Nejnovější"), 
    OLDEST("Nejstarší"), 
    NAME_AZ("Podle názvu (A-Z)") 
}

class HomeViewModel(
    private val repository: LinkRepository,
    private val appSettings: AppSettingsManager? = null
) : ViewModel() {

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(
        try {
            SortOrder.valueOf(appSettings?.lastSortOrder?.value ?: "NEWEST")
        } catch (e: Exception) { SortOrder.NEWEST }
    )
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _showUnreadOnly = MutableStateFlow(false)
    val showUnreadOnly: StateFlow<Boolean> = _showUnreadOnly.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categoriesWithCount: StateFlow<List<CategoryWithCount>> = repository.categoriesWithCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val rawLinks: StateFlow<List<LinkWithTagsAndCategory>> = repository.allLinks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val links: StateFlow<List<LinkWithTagsAndCategory>> = combine(
        rawLinks,
        _selectedCategoryId,
        _searchQuery,
        _sortOrder,
        _showUnreadOnly
    ) { allLinks, categoryId, query, order, unreadOnly ->
        var filtered = allLinks

        if (categoryId != null) {
            filtered = filtered.filter { it.link.categoryId == categoryId }
        }
        if (unreadOnly) {
            filtered = filtered.filter { !it.link.isRead }
        }
        if (query.isNotBlank()) {
            val q = query.lowercase()
            filtered = filtered.filter { 
                it.link.title.lowercase().contains(q) || 
                it.link.url.lowercase().contains(q) ||
                it.link.notes.lowercase().contains(q) ||
                it.tags.any { tag -> tag.name.lowercase().contains(q) }
            }
        }

        filtered = when (order) {
            SortOrder.NEWEST -> filtered.sortedByDescending { it.link.addedAt }
            SortOrder.OLDEST -> filtered.sortedBy { it.link.addedAt }
            SortOrder.NAME_AZ -> filtered.sortedBy { (it.link.title.takeIf { t -> t.isNotBlank() } ?: it.link.url).lowercase() }
        }

        val favorites = filtered.filter { it.link.isFavorite }
        val others = filtered.filter { !it.link.isFavorite }
        favorites + others
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dashboard Stats
    val totalLinksCount = rawLinks.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val favoritesCount = rawLinks.map { list -> list.count { it.link.isFavorite } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val unreadCount = rawLinks.map { list -> list.count { !it.link.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val recentLinks = rawLinks.map { list -> list.sortedByDescending { it.link.addedAt }.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Multi-select
    private val _selectedLinkIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedLinkIds: StateFlow<Set<Int>> = _selectedLinkIds.asStateFlow()

    fun toggleSelection(id: Int) {
        val current = _selectedLinkIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _selectedLinkIds.value = current
    }

    fun clearSelection() {
        _selectedLinkIds.value = emptySet()
    }

    fun deleteSelectedLinks() {
        viewModelScope.launch {
            repository.deleteLinks(_selectedLinkIds.value.toList())
            clearSelection()
        }
    }

    fun markSelectedAsRead(isRead: Boolean) {
        viewModelScope.launch {
            repository.updateReadStatus(_selectedLinkIds.value.toList(), isRead)
            clearSelection()
        }
    }

    fun onCategorySelected(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderChanged(order: SortOrder) {
        _sortOrder.value = order
        appSettings?.setLastSortOrder(order.name)
    }

    fun onShowUnreadOnlyChanged(show: Boolean) {
        _showUnreadOnly.value = show
    }

    fun toggleFavorite(link: Link) {
        viewModelScope.launch {
            repository.toggleFavorite(link)
        }
    }

    fun toggleReadStatus(link: Link) {
        viewModelScope.launch {
            repository.updateReadStatus(listOf(link.id), !link.isRead)
        }
    }

    private var recentlyDeletedLink: Link? = null
    private var recentlyDeletedTags: List<Int> = emptyList()

    fun deleteLink(link: Link) {
        viewModelScope.launch {
            // First find from current state if available, instead of collecting
            val tags = links.value.find { it.link.id == link.id }
            recentlyDeletedTags = tags?.tags?.map { it.id } ?: emptyList()
            recentlyDeletedLink = link
            repository.deleteLink(link)
        }
    }

    fun undoDelete() {
        val linkToRestore = recentlyDeletedLink ?: return
        viewModelScope.launch {
            repository.insertLink(linkToRestore.copy(id = 0), recentlyDeletedTags)
            recentlyDeletedLink = null
            recentlyDeletedTags = emptyList()
        }
    }

    private val _clipboardUrl = MutableStateFlow<String?>(null)
    val clipboardUrl: StateFlow<String?> = _clipboardUrl.asStateFlow()

    fun checkClipboardUrl(url: String) {
        // Fetch tags before deleting or something
        viewModelScope.launch {
            // First check if it's already in the state
            val exists = links.value.any { it.link.url == url }
            if (!exists) {
                _clipboardUrl.value = url
            } else {
                _clipboardUrl.value = null
            }
        }
    }

    fun dismissClipboardUrl() {
        _clipboardUrl.value = null
    }

    fun createSampleCategories() {
        viewModelScope.launch {
            val samples = listOf(
                Category(name = "Články", colorHex = "#42A5F5", iconName = "Article", sortOrder = 0),
                Category(name = "Videa", colorHex = "#EF5350", iconName = "VideoLibrary", sortOrder = 1),
                Category(name = "Zajímavosti", colorHex = "#66BB6A", iconName = "Lightbulb", sortOrder = 2),
                Category(name = "Práce", colorHex = "#AB47BC", iconName = "Work", sortOrder = 3)
            )
            samples.forEach {
                repository.insertCategory(it)
            }
        }
    }

    fun quickAddLink(url: String, onResult: (Boolean, String?) -> Unit) {
        val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url

        viewModelScope.launch {
            try {
                // Check if already exists
                val all = links.value.map { it.link }
                if (all.any { it.url == validUrl }) {
                    onResult(false, "Odkaz již existuje v knihovně.")
                    return@launch
                }

                val result = withContext(Dispatchers.IO) {
                    URL(validUrl).toURI()
                    val document = Jsoup.connect(validUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .followRedirects(true)
                        .get()

                    var title = document.select("meta[property=og:title]").attr("content")
                    if (title.isEmpty()) title = document.title()

                    var image = document.select("meta[property=og:image]").attr("content")
                    if (image.isEmpty()) image = document.select("img").firstOrNull()?.attr("src") ?: ""
                    if (image.isNotEmpty() && !image.startsWith("http")) {
                        val urlObj = URL(validUrl)
                        image = if (image.startsWith("/")) "${urlObj.protocol}://${urlObj.host}$image" else "${urlObj.protocol}://${urlObj.host}/$image"
                    }

                    var favicon = document.select("link[rel~=(?i)^(shortcut icon|icon)$]").attr("href")
                    if (favicon.isNotEmpty() && !favicon.startsWith("http")) {
                        val urlObj = URL(validUrl)
                        favicon = if (favicon.startsWith("/")) "${urlObj.protocol}://${urlObj.host}$favicon" else "${urlObj.protocol}://${urlObj.host}/$favicon"
                    }

                    Link(
                        url = validUrl,
                        title = title,
                        imageUrl = image,
                        faviconUrl = favicon,
                        notes = "",
                        isFavorite = false,
                        categoryId = null
                    )
                }

                repository.insertLink(result)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, "Přidání selhalo: ${e.message}")
            }
        }
    }

    class Factory(
        private val repository: LinkRepository,
        private val appSettings: AppSettingsManager? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repository, appSettings) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
