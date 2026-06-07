package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.LinklyApplication
import com.example.data.local.entity.Tag
import com.example.ui.utils.toColor

val premiumColorsList = listOf(
    "#EF5350", "#EC407A", "#AB47BC", "#7E57C2", 
    "#5C6BC0", "#42A5F5", "#26A69A", "#66BB6A", 
    "#FFA726", "#FF7043", "#8D6E63", "#78909C"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTagsScreen(
    onNavigateBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as LinklyApplication
    val viewModel: ManageTagsViewModel = viewModel(
        factory = ManageTagsViewModel.Factory(application.repository)
    )

    val tags by viewModel.tags.collectAsStateWithLifecycle()
    var editingTag by remember { mutableStateOf<Tag?>(null) }
    var isEditorOpen by remember { mutableStateOf(false) }
    var tagToDelete by remember { mutableStateOf<Tag?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Správa tagů") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    editingTag = Tag(name = "", colorHex = premiumColorsList.first())
                    isEditorOpen = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nový tag", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tags, key = { it.id }) { tag ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(tag.colorHex?.toColor() ?: MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = tag.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = {
                            editingTag = tag
                            isEditorOpen = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Upravit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { tagToDelete = tag }) {
                            Icon(Icons.Default.Delete, contentDescription = "Smazat", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        if (isEditorOpen && editingTag != null) {
            TagEditorDialog(
                tag = editingTag!!,
                onDismiss = { 
                    isEditorOpen = false
                    editingTag = null
                },
                onSave = { updatedTag ->
                    viewModel.saveTag(updatedTag)
                    isEditorOpen = false
                    editingTag = null
                }
            )
        }

        if (tagToDelete != null) {
            AlertDialog(
                onDismissRequest = { tagToDelete = null },
                title = { Text("Smazat tag?") },
                text = { Text("Opravdu chcete smazat tag '${tagToDelete?.name}'? Odkazy s tímto tagem zůstanou zachovány, pouze přijdou o tento tag.") },
                confirmButton = {
                    TextButton(onClick = {
                        tagToDelete?.let { viewModel.deleteTag(it) }
                        tagToDelete = null
                    }) {
                        Text("Smazat", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { tagToDelete = null }) {
                        Text("Zrušit")
                    }
                }
            )
        }
    }
}

@Composable
fun TagEditorDialog(
    tag: Tag,
    onDismiss: () -> Unit,
    onSave: (Tag) -> Unit
) {
    var name by remember { mutableStateOf(tag.name) }
    var colorHex by remember { mutableStateOf(tag.colorHex ?: premiumColorsList.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (tag.id == 0) "Nový tag" else "Upravit tag") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Název") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Barva", style = MaterialTheme.typography.labelMedium)
                ColorPickerGrid(selectedColorHex = colorHex, onColorSelected = { colorHex = it })
                
                // Prieview
                Surface(
                    color = colorHex.toColor().copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = name.ifBlank { "Náhled tagu" },
                        color = colorHex.toColor(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(tag.copy(name = name.trim(), colorHex = colorHex)) },
                enabled = name.isNotBlank()
            ) {
                Text("Uložit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerGrid(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        premiumColorsList.forEach { hex ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(hex.toColor())
                    .clickable { onColorSelected(hex) },
                contentAlignment = Alignment.Center
            ) {
                if (hex == selectedColorHex) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary)
                    )
                }
            }
        }
    }
}
