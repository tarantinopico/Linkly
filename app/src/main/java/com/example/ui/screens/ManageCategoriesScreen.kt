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
import com.example.data.local.entity.Category
import com.example.ui.utils.availableIcons
import com.example.ui.utils.toColor
import com.example.ui.utils.toIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    onNavigateBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as LinklyApplication
    val viewModel: ManageCategoriesViewModel = viewModel(
        factory = ManageCategoriesViewModel.Factory(application.repository)
    )

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var isEditorOpen by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Správa kategorií") },
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
                    editingCategory = Category(name = "", colorHex = premiumColorsList.first(), iconName = "Folder", sortOrder = categories.size)
                    isEditorOpen = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nová kategorie", tint = MaterialTheme.colorScheme.onPrimary)
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
            items(categories, key = { it.id }) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = category.colorHex.toColor().copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = category.iconName.toIcon(),
                                contentDescription = null,
                                tint = category.colorHex.toColor(),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = category.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = {
                            editingCategory = category
                            isEditorOpen = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Upravit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { categoryToDelete = category }) {
                            Icon(Icons.Default.Delete, contentDescription = "Smazat", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        if (isEditorOpen && editingCategory != null) {
            CategoryEditorDialog(
                category = editingCategory!!,
                onDismiss = { 
                    isEditorOpen = false
                    editingCategory = null
                },
                onSave = { updatedCategory ->
                    viewModel.saveCategory(updatedCategory)
                    isEditorOpen = false
                    editingCategory = null
                }
            )
        }

        if (categoryToDelete != null) {
            AlertDialog(
                onDismissRequest = { categoryToDelete = null },
                title = { Text("Smazat kategorii?") },
                text = { Text("Opravdu chcete smazat kategorii '${categoryToDelete?.name}'? Všechny odkazy v této kategorii zůstanou uloženy, ale přeřadí se do 'Nezarazeno'.") },
                confirmButton = {
                    TextButton(onClick = {
                        categoryToDelete?.let { viewModel.deleteCategory(it) }
                        categoryToDelete = null
                    }) {
                        Text("Smazat", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToDelete = null }) {
                        Text("Zrušit")
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryEditorDialog(
    category: Category,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    var colorHex by remember { mutableStateOf(category.colorHex) }
    var iconName by remember { mutableStateOf(category.iconName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category.id == 0) "Nová kategorie" else "Upravit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Název") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Ikona", style = MaterialTheme.typography.labelMedium)
                IconPickerGrid(selectedIconName = iconName, onIconSelected = { iconName = it })

                Text("Barva", style = MaterialTheme.typography.labelMedium)
                ColorPickerGrid(selectedColorHex = colorHex, onColorSelected = { colorHex = it })
                
                // Prieview
                Surface(
                    color = colorHex.toColor().copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(iconName.toIcon(), contentDescription = null, tint = colorHex.toColor(), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = name.ifBlank { "Náhled" },
                            color = colorHex.toColor(),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(category.copy(name = name.trim(), colorHex = colorHex, iconName = iconName)) },
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
fun IconPickerGrid(
    selectedIconName: String,
    onIconSelected: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        availableIcons.keys.take(16).forEach { name -> // Limit to 16 for UI space
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (name == selectedIconName) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onIconSelected(name) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = name.toIcon(),
                    contentDescription = null,
                    tint = if (name == selectedIconName) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
