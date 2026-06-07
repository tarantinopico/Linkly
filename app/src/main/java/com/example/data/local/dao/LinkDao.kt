package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.Link
import com.example.data.local.entity.LinkTagCrossRef
import com.example.data.local.entity.LinkWithTagsAndCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkDao {

    @Transaction
    @Query("SELECT * FROM links ORDER BY addedAt DESC")
    fun getAllLinks(): Flow<List<LinkWithTagsAndCategory>>

    @Transaction
    @Query("SELECT * FROM links WHERE categoryId = :categoryId ORDER BY addedAt DESC")
    fun getLinksByCategory(categoryId: Int): Flow<List<LinkWithTagsAndCategory>>

    @Transaction
    @Query("SELECT * FROM links INNER JOIN link_tag_cross_ref ON links.id = link_tag_cross_ref.linkId WHERE tagId = :tagId ORDER BY addedAt DESC")
    fun getLinksByTag(tagId: Int): Flow<List<LinkWithTagsAndCategory>>

    @Transaction
    @Query("SELECT * FROM links WHERE id = :linkId")
    fun getLinkById(linkId: Int): Flow<LinkWithTagsAndCategory?>

    @Query("SELECT * FROM links")
    suspend fun getAllLinksList(): List<Link>

    @Query("SELECT * FROM link_tag_cross_ref")
    suspend fun getAllCrossRefsList(): List<LinkTagCrossRef>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<Link>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinkTagCrossRefs(crossRefs: List<LinkTagCrossRef>)

    @Query("DELETE FROM links")
    suspend fun deleteAllLinks()

    @Query("DELETE FROM link_tag_cross_ref")
    suspend fun deleteAllCrossRefs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: Link): Long

    @Update
    suspend fun updateLink(link: Link)

    @Delete
    suspend fun deleteLink(link: Link)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLinkTagCrossRef(crossRef: LinkTagCrossRef)
    
    @Delete
    suspend fun deleteLinkTagCrossRef(crossRef: LinkTagCrossRef)
    
    @Query("DELETE FROM link_tag_cross_ref WHERE linkId = :linkId")
    suspend fun deleteTagsForLink(linkId: Int)
}
