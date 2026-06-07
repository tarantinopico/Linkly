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

import com.example.utils.AppSettingsManager

class AddEditLinkViewModel(
    private val repository: LinkRepository,
    private val linkId: Int = -1,
    private val sharedUrl: String? = null,
    private val appSettings: AppSettingsManager? = null
) : ViewModel() {

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _imageUrl = MutableStateFlow<String?>("")
    val imageUrl: StateFlow<String?> = _imageUrl.asStateFlow()

    private val _faviconUrl = MutableStateFlow<String?>("")
    val faviconUrl: StateFlow<String?> = _faviconUrl.asStateFlow()

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
                    _faviconUrl.value = it.link.faviconUrl
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

    private var lastAutoAddedTag: Tag? = null

    private fun checkAutoTagging() {
        val globalEnabled = appSettings?.isAutoTaggingEnabled?.value ?: true
        if (!globalEnabled) return

        viewModelScope.launch {
            val urlString = _url.value.trim()
            if (urlString.isEmpty()) return@launch

            val categoryId = _selectedCategoryId.value
            if (categoryId != null) {
                val category = availableCategories.value.find { it.id == categoryId }
                if (category?.isAutoTaggingEnabled == false) {
                    return@launch
                }
            }

            try {
                val host = URL(if (!urlString.startsWith("http")) "https://$urlString" else urlString).host.lowercase()
                val rules = repository.allAutoTagRules.firstOrNull() ?: emptyList()
                
                val matchedRule = rules.find { host.contains(it.domain.lowercase()) }
                if (matchedRule != null) {
                    val tagName = matchedRule.tagName
                    var tag = availableTags.value.find { it.name.equals(tagName, ignoreCase = true) }
                    if (tag == null) {
                        tag = repository.getTagByName(tagName)
                        if (tag == null) {
                            val colors = listOf("#EF5350", "#EC407A", "#AB47BC", "#7E57C2", "#5C6BC0", "#42A5F5", "#26A69A", "#66BB6A", "#FFA726", "#FF7043")
                            val newTag = Tag(name = tagName, colorHex = colors.random())
                            val id = repository.insertTag(newTag)
                            tag = newTag.copy(id = id.toInt())
                        }
                    }
                    if (!_selectedTags.value.contains(tag) && lastAutoAddedTag != tag) {
                        _selectedTags.value = _selectedTags.value + tag
                        lastAutoAddedTag = tag
                    }
                }
            } catch (e: Exception) {
                // Invalid URL
            }
        }
    }

    fun onUrlChange(newUrl: String) {
        _url.value = newUrl
        _metadataError.value = null
        checkAutoTagging()
    }

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onNotesChange(newNotes: String) {
        _notes.value = newNotes
    }

    fun onCategoryChange(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
        checkAutoTagging()
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
                    val document = Jsoup.connect(validUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
                        .timeout(15000)
                        .followRedirects(true)
                        .ignoreContentType(true)
                        .get()

                    var title = document.select("meta[property=og:title]").attr("content")
                    if (title.isEmpty()) title = document.select("meta[name=twitter:title]").attr("content")
                    if (title.isEmpty()) title = document.title()
                    if (title.isEmpty()) {
                        val host = URL(validUrl).host
                        title = if (host.startsWith("www.")) host.substring(4) else host
                    }

                    var image = document.select("meta[property=og:image]").attr("content")
                    if (image.isEmpty()) image = document.select("meta[name=twitter:image]").attr("content")
                    if (image.isEmpty()) {
                        val imgAttr = document.select("img").firstOrNull()?.attr("src") ?: ""
                        image = imgAttr
                    }
                    if (image.isNotEmpty() && !image.startsWith("http")) {
                        val urlObj = URL(validUrl)
                        image = if (image.startsWith("/")) {
                            "${urlObj.protocol}://${urlObj.host}$image"
                        } else {
                            "${urlObj.protocol}://${urlObj.host}/$image"
                        }
                    }

                    var favicon = document.select("link[rel~=(?i)^(shortcut icon|icon)$]").attr("href")
                    if (favicon.isEmpty()) favicon = document.select("link[rel=apple-touch-icon]").attr("href")
                    if (favicon.isNotEmpty() && !favicon.startsWith("http")) {
                        val urlObj = URL(validUrl)
                        favicon = if (favicon.startsWith("/")) {
                            "${urlObj.protocol}://${urlObj.host}$favicon"
                        } else {
                            "${urlObj.protocol}://${urlObj.host}/$favicon"
                        }
                    }
                    if (favicon.isEmpty()) {
                        val urlObj = URL(validUrl)
                        favicon = "https://www.google.com/s2/favicons?sz=128&domain=${urlObj.host}"
                    }

                    withContext(Dispatchers.Main) {
                        if (_title.value.isBlank()) _title.value = title
                        if (_imageUrl.value.isNullOrBlank() && image.isNotEmpty()) _imageUrl.value = image
                        if (_faviconUrl.value.isNullOrBlank() && favicon.isNotEmpty()) _faviconUrl.value = favicon
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
                faviconUrl = _faviconUrl.value,
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
        private val sharedUrl: String?,
        private val appSettings: AppSettingsManager? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddEditLinkViewModel(repository, linkId, sharedUrl, appSettings) as T
        }
    }
}
