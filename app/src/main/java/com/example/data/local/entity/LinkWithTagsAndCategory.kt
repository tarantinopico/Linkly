package com.example.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.Relation

@Entity(
    tableName = "link_tag_cross_ref",
    primaryKeys = ["linkId", "tagId"],
    indices = [Index("tagId")]
)
data class LinkTagCrossRef(
    val linkId: Int,
    val tagId: Int
)

data class LinkWithTagsAndCategory(
    @Embedded val link: Link,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(LinkTagCrossRef::class, parentColumn = "linkId", entityColumn = "tagId")
    )
    val tags: List<Tag>
)
