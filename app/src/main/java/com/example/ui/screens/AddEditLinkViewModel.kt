package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Category
import com.example.data.local.entity.Link
import com.example.data.local.entity.Tag
import com.example.data.repository.LinkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URL

class AddEditLinkViewModel(
    private val repository: LinkRepository,
    private val linkId: Int = -1,
    private val sharedUrl: String? = null
) : ViewModel() {

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _imageUrl = MutableStateFlow<String?>("")
    val imageUrl: StateFlow<String?> = _imageUrl.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _selectedTags = MutableStateFlow<List<Tag>>(emptyList())
    val selectedTags: StateFlow<List<Tag>> = _selectedTags.asStateFlow()

    private val _isLoadingMetadata = MutableStateFlow(false)
    val isLoadingMetadata: StateFlow<Boolean> = _isLoadingMetadata.asStateFlow()

    private val _metadataError = MutableStateFlow<String?>(null)
    val metadataError: StateFlow<String?> = _metadataError.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    val availableCategories = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val availableTags = repository.allTags.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        if (linkId != -1) {
            viewModelScope.launch {
                val linkWithDetails = repository.getLinkById(linkId).firstOrNull()
                linkWithDetails?.let {
                    _url.value = it.link.url
                    _title.value = it.link.title
                    _imageUrl.value = it.link.imageUrl
                    _notes.value = it.link.notes
                    _selectedCategoryId.value = it.link.categoryId
                    _selectedTags.value = it.tags
                }
            }
        } else if (!sharedUrl.isNullOrEmpty()) {
            _url.value = sharedUrl
            fetchMetadata()
        }
    }

    fun onUrlChange(newUrl: String) {
        _url.value = newUrl
        _metadataError.value = null
    }

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onNotesChange(newNotes: String) {
        _notes.value = newNotes
    }

    fun onCategoryChange(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }

    fun addTag(tag: Tag) {
        if (!_selectedTags.value.contains(tag)) {
            _selectedTags.value = _selectedTags.value + tag
        }
    }

    fun removeTag(tag: Tag) {
        _selectedTags.value = _selectedTags.value - tag
    }

    fun createAndAddTag(tagName: String) {
        if (tagName.isBlank()) return
        viewModelScope.launch {
            val trimmedName = tagName.trim()
            val existingInAvailable = availableTags.value.find { it.name.equals(trimmedName, ignoreCase = true) }
            if (existingInAvailable != null) {
                addTag(existingInAvailable)
                return@launch
            }

            var dbTag = repository.getTagByName(trimmedName)
            if (dbTag == null) {
                val colors = listOf("#EF5350", "#EC407A", "#AB47BC", "#7E57C2", "#5C6BC0", "#42A5F5", "#26A69A", "#66BB6A", "#FFA726", "#FF7043")
                val newTag = Tag(name = trimmedName, colorHex = colors.random())
                val id = repository.insertTag(newTag)
                dbTag = newTag.copy(id = id.toInt())
            }
            addTag(dbTag)
        }
    }

    fun fetchMetadata() {
        val currentUrl = _url.value.trim()
        if (currentUrl.isEmpty()) {
            _metadataError.value = "Zadejte URL adresu."
            return
        }

        val validUrl = if (!currentUrl.startsWith("http://") && !currentUrl.startsWith("https://")) {
            "https://$currentUrl"
        } else {
            currentUrl
        }
        _url.value = validUrl

        viewModelScope.launch {
            _isLoadingMetadata.value = true
            _metadataError.value = null

            try {
                withContext(Dispatchers.IO) {
                    URL(validUrl).toURI() // check syntax
                    val document = Jsoup.connect(validUrl).timeout(5000).get()

                    val ogTitle = document.select("meta[property=og:title]").attr("content")
                    val title = ogTitle.takeIf { it.isNotEmpty() } ?: document.title()

                    val ogImage = document.select("meta[property=og:image]").attr("content")
                    val image = ogImage.takeIf { it.isNotEmpty() } ?: document.select("link[rel=apple-touch-icon]").attr("href")

                    withContext(Dispatchers.Main) {
                        if (title.isNotEmpty()) _title.value = title
                        if (image.isNotEmpty()) {
                            _imageUrl.value = if (image.startsWith("/")) {
                                val urlObj = URL(validUrl)
                                "${urlObj.protocol}://${urlObj.host}$image"
                            } else {
                                image
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _metadataError.value = "Nepodařilo se stáhnout metadata. Zkontrolujte URL."
            } finally {
                _isLoadingMetadata.value = false
            }
        }
    }

    fun saveLink() {
        if (_url.value.isBlank()) return

        viewModelScope.launch {
            val link = Link(
                id = if (linkId != -1) linkId else 0,
                url = _url.value.trim(),
                title = _title.value.trim(),
                imageUrl = _imageUrl.value,
                notes = _notes.value.trim(),
                categoryId = _selectedCategoryId.value
            )

            val tagIds = _selectedTags.value.map { it.id }

            if (linkId != -1) {
                repository.updateLink(link, tagIds)
            } else {
                repository.insertLink(link, tagIds)
            }
            _isSaved.value = true
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val repository: LinkRepository,
        private val linkId: Int,
        private val sharedUrl: String?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddEditLinkViewModel(repository, linkId, sharedUrl) as T
        }
    }
}
