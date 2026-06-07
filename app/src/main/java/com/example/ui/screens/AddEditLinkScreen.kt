package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.LinklyApplication
import com.example.data.local.entity.Category
import com.example.data.local.entity.Tag
import com.example.ui.utils.toColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditLinkScreen(
    linkId: Int = -1,
    sharedUrl: String? = null,
    onNavigateBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as LinklyApplication
    val viewModel: AddEditLinkViewModel = viewModel(
        factory = AddEditLinkViewModel.Factory(application.repository, linkId, sharedUrl)
    )

    val url by viewModel.url.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val imageUrl by viewModel.imageUrl.collectAsStateWithLifecycle()
    val faviconUrl by viewModel.faviconUrl.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val selectedTags by viewModel.selectedTags.collectAsStateWithLifecycle()
    
    val isLoadingMetadata by viewModel.isLoadingMetadata.collectAsStateWithLifecycle()
    val metadataError by viewModel.metadataError.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()

    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()

    LaunchedEffect(isSaved) {
        if (isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (linkId == -1) "Přidat odkaz" else "Upravit odkaz") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveLink() }, enabled = url.isNotBlank()) {
                        Icon(Icons.Default.Save, contentDescription = "Uložit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // URL Input
            OutlinedTextField(
                value = url,
                onValueChange = viewModel::onUrlChange,
                label = { Text("URL adresa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.fetchMetadata() }
                ),
                trailingIcon = {
                    IconButton(onClick = { viewModel.fetchMetadata() }) {
                        Icon(Icons.Default.CloudDownload, contentDescription = "Stáhnout metadata")
                    }
                },
                isError = metadataError != null,
                supportingText = metadataError?.let { { Text(it) } }
            )

            if (isLoadingMetadata) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            }

            AnimatedVisibility(visible = !imageUrl.isNullOrBlank() || !faviconUrl.isNullOrBlank() || title.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column {
                        val imageToLoad = if (!imageUrl.isNullOrBlank()) imageUrl else faviconUrl
                        if (!imageToLoad.isNullOrBlank()) {
                            SubcomposeAsyncImage(
                                model = imageToLoad,
                                contentDescription = "Live Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(MaterialTheme.colorScheme.surface)
                            )
                        }
                        if (title.isNotBlank()) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp),
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Název odkazu") },
                modifier = Modifier.fillMaxWidth()
            )

            CategoryDropdown(
                categories = availableCategories,
                selectedId = selectedCategoryId,
                onSelected = viewModel::onCategoryChange
            )

            TagsInputSection(
                selectedTags = selectedTags,
                availableTags = availableTags,
                onAddTag = viewModel::addTag,
                onRemoveTag = viewModel::removeTag,
                onCreateTag = viewModel::createAndAddTag
            )

            OutlinedTextField(
                value = notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Poznámka") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                maxLines = 5
            )
            
            Button(
                onClick = { viewModel.saveLink() },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = url.isNotBlank()
            ) {
                Text(if (linkId == -1) "Uložit odkaz" else "Uložit změny", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<Category>,
    selectedId: Int?,
    onSelected: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == selectedId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "Zvolte kategorii",
            onValueChange = {},
            readOnly = true,
            label = { Text("Kategorie") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Žádná") },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    leadingIcon = {
                        Box(modifier = Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(category.colorHex.toColor()))
                    },
                    onClick = {
                        onSelected(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsInputSection(
    selectedTags: List<Tag>,
    availableTags: List<Tag>,
    onAddTag: (Tag) -> Unit,
    onRemoveTag: (Tag) -> Unit,
    onCreateTag: (String) -> Unit
) {
    var newTagText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tagy", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

        if (selectedTags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedTags.forEach { tag ->
                    InputChip(
                        selected = true,
                        onClick = { onRemoveTag(tag) },
                        label = { Text(tag.name) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = tag.colorHex?.toColor()?.copy(alpha = 0.2f) ?: MaterialTheme.colorScheme.surfaceVariant,
                            selectedLabelColor = tag.colorHex?.toColor() ?: MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        OutlinedTextField(
            value = newTagText,
            onValueChange = { newTagText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Nový tag...") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    onCreateTag(newTagText)
                    newTagText = ""
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Tag")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onCreateTag(newTagText)
                newTagText = ""
            })
        )

        val unselectedTags = availableTags.filter { it !in selectedTags }
        if (unselectedTags.isNotEmpty()) {
            Text("Dostupné tagy:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                unselectedTags.forEach { tag ->
                    SuggestionChip(
                        onClick = { onAddTag(tag) },
                        label = { Text(tag.name) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = tag.colorHex?.toColor() ?: MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}
