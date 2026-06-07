package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Category
import com.example.data.local.entity.Link
import com.example.data.local.entity.LinkWithTagsAndCategory
import com.example.data.repository.LinkRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOrder(val displayName: String) { 
    NEWEST("Nejnovější"), 
    OLDEST("Nejstarší"), 
    NAME_AZ("Podle názvu (A-Z)") 
}

class HomeViewModel(private val repository: LinkRepository) : ViewModel() {

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val links: StateFlow<List<LinkWithTagsAndCategory>> = combine(
        repository.allLinks,
        _selectedCategoryId,
        _searchQuery,
        _sortOrder
    ) { allLinks, categoryId, query, order ->
        var filtered = allLinks

        if (categoryId != null) {
            filtered = filtered.filter { it.link.categoryId == categoryId }
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

    fun onCategorySelected(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderChanged(order: SortOrder) {
        _sortOrder.value = order
    }

    fun toggleFavorite(link: Link) {
        viewModelScope.launch {
            repository.toggleFavorite(link)
        }
    }

    fun deleteLink(link: Link) {
        viewModelScope.launch {
            repository.deleteLink(link)
        }
    }

    class Factory(private val repository: LinkRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
