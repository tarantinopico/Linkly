package com.example.data.repository

import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.CategoryWithCount
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
    // ---- Backup & Restore ----
    data class RawBackupData(
        val categories: List<Category>,
        val tags: List<Tag>,
        val links: List<Link>,
        val crossRefs: List<LinkTagCrossRef>
    )

    suspend fun exportRawData(): RawBackupData {
        return RawBackupData(
            categoryDao.getAllCategoriesList(),
            tagDao.getAllTagsList(),
            linkDao.getAllLinksList(),
            linkDao.getAllCrossRefsList()
        )
    }

    suspend fun restoreRawData(data: RawBackupData) {
        // Obnovení s přeplněním - mažeme a dáváme nové pro zachování ID vazeb
        linkDao.deleteAllCrossRefs()
        linkDao.deleteAllLinks()
        tagDao.deleteAllTags()
        categoryDao.deleteAllCategories()

        categoryDao.insertCategories(data.categories)
        tagDao.insertTags(data.tags)
        linkDao.insertLinks(data.links)
        linkDao.insertLinkTagCrossRefs(data.crossRefs)
    }

    // Links
    val allLinks: Flow<List<LinkWithTagsAndCategory>> = linkDao.getAllLinks()

    val categoriesWithCount = categoryDao.getCategoriesWithCount()

    suspend fun updateReadStatus(linkIds: List<Int>, isRead: Boolean) {
        linkDao.updateReadStatus(linkIds, isRead)
    }

    suspend fun updateCategoryForLinks(linkIds: List<Int>, categoryId: Int?) {
        linkDao.updateCategoryForLinks(linkIds, categoryId)
    }

    suspend fun deleteLinks(linkIds: List<Int>) {
        linkDao.deleteLinks(linkIds)
    }

    fun getLinksByCategory(categoryId: Int) = linkDao.getLinksByCategory(categoryId)
    fun getLinksByTag(tagId: Int) = linkDao.getLinksByTag(tagId)
    fun getLinkById(linkId: Int) = linkDao.getLinkById(linkId)

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

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }
    
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    // Tags
    val allTags: Flow<List<Tag>> = tagDao.getAllTags()
    
    suspend fun getTagByName(name: String): Tag? = tagDao.getTagByName(name)

    suspend fun insertTag(tag: Tag): Long {
        return tagDao.insertTag(tag)
    }

    suspend fun updateTag(tag: Tag) {
        tagDao.updateTag(tag)
    }
    
    suspend fun deleteTag(tag: Tag) {
        tagDao.deleteTag(tag)
    }
}
