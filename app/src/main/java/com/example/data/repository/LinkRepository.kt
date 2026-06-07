package com.example.data.repository

import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.LinkDao
import com.example.data.local.dao.TagDao
import com.example.data.local.entity.Category
import com.example.data.local.entity.Link
import com.example.data.local.entity.LinkTagCrossRef
import com.example.data.local.entity.LinkWithTagsAndCategory
import com.example.data.local.entity.Tag
import kotlinx.coroutines.flow.Flow

class LinkRepository(
    private val linkDao: LinkDao,
    private val categoryDao: CategoryDao,
    private val tagDao: TagDao
) {
    // Links
    val allLinks: Flow<List<LinkWithTagsAndCategory>> = linkDao.getAllLinks()

    fun getLinksByCategory(categoryId: Int) = linkDao.getLinksByCategory(categoryId)
    fun getLinksByTag(tagId: Int) = linkDao.getLinksByTag(tagId)

    suspend fun insertLink(link: Link, tagIds: List<Int> = emptyList()): Int {
        val linkId = linkDao.insertLink(link).toInt()
        tagIds.forEach { tagId ->
            linkDao.insertLinkTagCrossRef(LinkTagCrossRef(linkId, tagId))
        }
        return linkId
    }

    suspend fun updateLink(link: Link, newTagIds: List<Int> = emptyList()) {
        linkDao.updateLink(link)
        linkDao.deleteTagsForLink(link.id)
        newTagIds.forEach { tagId ->
            linkDao.insertLinkTagCrossRef(LinkTagCrossRef(link.id, tagId))
        }
    }

    suspend fun deleteLink(link: Link) {
        linkDao.deleteLink(link)
    }

    suspend fun toggleFavorite(link: Link) {
        linkDao.updateLink(link.copy(isFavorite = !link.isFavorite))
    }

    // Categories
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    
    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }
    
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    // Tags
    val allTags: Flow<List<Tag>> = tagDao.getAllTags()
    
    suspend fun insertTag(tag: Tag) {
        tagDao.insertTag(tag)
    }
    
    suspend fun deleteTag(tag: Tag) {
        tagDao.deleteTag(tag)
    }
}
