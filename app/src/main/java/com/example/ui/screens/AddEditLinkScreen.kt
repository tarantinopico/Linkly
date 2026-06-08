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
import com.example.ui.utils.shimmerEffect

import com.example.ui.utils.premiumBackground
import com.example.ui.utils.premiumCardStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditLinkScreen(
    linkId: Int = -1,
    sharedUrl: String? = null,
    onNavigateBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as LinklyApplication
    val viewModel: AddEditLinkViewModel = viewModel(
        factory = AddEditLinkViewModel.Factory(application.repository, linkId, sharedUrl, application.appSettings)
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
                title = { Text(if (linkId == -1) "Přidat odkaz" else "Upravit odkaz", fontWeight = FontWeight.Bold) },
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
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = com.example.ui.theme.TextPrimary,
                    actionIconContentColor = com.example.ui.theme.TextPrimary,
                    navigationIconContentColor = com.example.ui.theme.TextPrimary
                )
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.premiumBackground()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
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
                supportingText = metadataError?.let { { Text(it) } },
                shape = RoundedCornerShape(12.dp)
            )

            if (isLoadingMetadata) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp).premiumCardStyle(containerColor = com.example.ui.theme.CardSurfaceLight, shadowElevation = 0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                }
            } else {
                AnimatedVisibility(visible = !imageUrl.isNullOrBlank() || !faviconUrl.isNullOrBlank() || title.isNotBlank()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().premiumCardStyle(containerColor = com.example.ui.theme.CardSurfaceDark, shadowElevation = 8.dp)
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
                                        .height(160.dp)
                                        .background(com.example.ui.theme.CardSurfaceLight)
                                )
                            }
                            if (title.isNotBlank()) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(16.dp),
                                    color = com.example.ui.theme.TextPrimary,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Název odkazu") },
                shape = RoundedCornerShape(12.dp),
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
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 5
            )
            
            Button(
                onClick = { viewModel.saveLink() },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 40.dp).height(56.dp),
                enabled = url.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.MutedPurple),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (linkId == -1) "Uložit odkaz" else "Uložit změny", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
            shape = RoundedCornerShape(12.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(com.example.ui.theme.CardSurfaceLight)
        ) {
            DropdownMenuItem(
                text = { Text("Žádná", color = com.example.ui.theme.TextPrimary) },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name, color = com.example.ui.theme.TextPrimary) },
                    leadingIcon = {
                        Box(modifier = Modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(category.colorHex.toColor()))
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Tagy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.TextPrimary)

        if (selectedTags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedTags.forEach { tag ->
                    InputChip(
                        selected = true,
                        onClick = { onRemoveTag(tag) },
                        label = { Text(tag.name, fontWeight = FontWeight.Medium) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = tag.colorHex?.toColor()?.copy(alpha = 0.2f) ?: com.example.ui.theme.CardSurfaceDark,
                            selectedLabelColor = tag.colorHex?.toColor() ?: com.example.ui.theme.TextPrimary
                        ),
                        border = InputChipDefaults.inputChipBorder(
                            borderColor = tag.colorHex?.toColor()?.copy(alpha=0.3f) ?: com.example.ui.theme.CardBorder,
                            enabled = true, selected = true
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
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onCreateTag(newTagText)
                newTagText = ""
            })
        )

        val unselectedTags = availableTags.filter { it !in selectedTags }
        if (unselectedTags.isNotEmpty()) {
            Text("Dostupné tagy:", style = MaterialTheme.typography.labelLarge, color = com.example.ui.theme.TextSecondary, modifier = Modifier.padding(top = 8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                unselectedTags.forEach { tag ->
                    SuggestionChip(
                        onClick = { onAddTag(tag) },
                        label = { Text(tag.name) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = com.example.ui.theme.CardSurfaceDark,
                            labelColor = tag.colorHex?.toColor() ?: com.example.ui.theme.TextPrimary
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = tag.colorHex?.toColor()?.copy(alpha=0.3f) ?: com.example.ui.theme.CardBorder
                        )
                    )
                }
            }
        }
    }
}
