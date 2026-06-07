package com.example.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import com.example.data.local.entity.Category
import com.example.data.local.entity.Link
import com.example.data.local.entity.LinkTagCrossRef
import com.example.data.local.entity.Tag

class BackupRestoreManager(
    private val context: Context,
    private val repository: LinkRepository
) {
    suspend fun exportData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val data = repository.exportRawData()

            val json = JSONObject()
            
            // Categories
            val catsArray = JSONArray()
            data.categories.forEach { c ->
                val obj = JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("colorHex", c.colorHex)
                    put("iconName", c.iconName)
                    put("sortOrder", c.sortOrder)
                }
                catsArray.put(obj)
            }
            json.put("categories", catsArray)

            // Tags
            val tagsArray = JSONArray()
            data.tags.forEach { t ->
                val obj = JSONObject().apply {
                    put("id", t.id)
                    put("name", t.name)
                    put("colorHex", t.colorHex ?: JSONObject.NULL)
                }
                tagsArray.put(obj)
            }
            json.put("tags", tagsArray)

            // Links
            val linksArray = JSONArray()
            data.links.forEach { l ->
                val obj = JSONObject().apply {
                    put("id", l.id)
                    put("url", l.url)
                    put("title", l.title)
                    put("imageUrl", l.imageUrl ?: JSONObject.NULL)
                    put("notes", l.notes)
                    put("categoryId", l.categoryId ?: JSONObject.NULL)
                    put("addedAt", l.addedAt)
                    put("isFavorite", l.isFavorite)
                }
                linksArray.put(obj)
            }
            json.put("links", linksArray)

            // CrossRefs
            val crossRefsArray = JSONArray()
            data.crossRefs.forEach { cr ->
                val obj = JSONObject().apply {
                    put("linkId", cr.linkId)
                    put("tagId", cr.tagId)
                }
                crossRefsArray.put(obj)
            }
            json.put("cross_refs", crossRefsArray)

            context.contentResolver.openOutputStream(uri)?.use { output ->
                OutputStreamWriter(output).use { writer ->
                    writer.write(json.toString(2))
                }
            } ?: throw Exception("Nelze otevřít soubor pro zápis.")

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { input ->
                InputStreamReader(input).readText()
            } ?: throw Exception("Nelze načíst soubor.")

            val json = JSONObject(jsonString)
            
            // Parse Categories
            val categories = mutableListOf<Category>()
            val catsArray = json.optJSONArray("categories")
            if (catsArray != null) {
                for (i in 0 until catsArray.length()) {
                    val obj = catsArray.getJSONObject(i)
                    categories.add(Category(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        colorHex = obj.getString("colorHex"),
                        iconName = obj.getString("iconName"),
                        sortOrder = obj.optInt("sortOrder", 0)
                    ))
                }
            }

            // Parse Tags
            val tags = mutableListOf<Tag>()
            val tagsArray = json.optJSONArray("tags")
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    val obj = tagsArray.getJSONObject(i)
                    tags.add(Tag(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        colorHex = if (obj.isNull("colorHex")) null else obj.getString("colorHex")
                    ))
                }
            }

            // Parse Links
            val links = mutableListOf<Link>()
            val linksArray = json.optJSONArray("links")
            if (linksArray != null) {
                for (i in 0 until linksArray.length()) {
                    val obj = linksArray.getJSONObject(i)
                    links.add(Link(
                        id = obj.getInt("id"),
                        url = obj.getString("url"),
                        title = obj.getString("title"),
                        imageUrl = if (obj.isNull("imageUrl")) null else obj.getString("imageUrl"),
                        notes = obj.optString("notes"),
                        categoryId = if (obj.isNull("categoryId")) null else obj.getInt("categoryId"),
                        addedAt = obj.getLong("addedAt"),
                        isFavorite = obj.optBoolean("isFavorite", false)
                    ))
                }
            }

            // Parse CrossRefs
            val crossRefs = mutableListOf<LinkTagCrossRef>()
            val crossRefsArray = json.optJSONArray("cross_refs")
            if (crossRefsArray != null) {
                for (i in 0 until crossRefsArray.length()) {
                    val obj = crossRefsArray.getJSONObject(i)
                    crossRefs.add(LinkTagCrossRef(
                        linkId = obj.getInt("linkId"),
                        tagId = obj.getInt("tagId")
                    ))
                }
            }

            val rawData = LinkRepository.RawBackupData(categories, tags, links, crossRefs)
            repository.restoreRawData(rawData)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
