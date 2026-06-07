package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Link
import com.example.data.repository.LinkRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LinkDetailViewModel(
    private val repository: LinkRepository,
    private val linkId: Int
) : ViewModel() {

    val linkDetails = repository.getLinkById(linkId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

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

    class Factory(private val repository: LinkRepository, private val linkId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LinkDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LinkDetailViewModel(repository, linkId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
