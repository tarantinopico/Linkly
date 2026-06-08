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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.LinklyApplication
import com.example.data.local.entity.Category
import com.example.ui.utils.availableIcons
import com.example.ui.utils.toColor
import com.example.ui.utils.toIcon
import com.example.ui.utils.premiumBackground
import com.example.ui.utils.premiumCardStyle

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
                title = { Text("Správa kategorií", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = com.example.ui.theme.TextPrimary,
                    navigationIconContentColor = com.example.ui.theme.TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    editingCategory = Category(name = "", colorHex = premiumColorsList.first(), iconName = "Folder", sortOrder = categories.size)
                    isEditorOpen = true
                },
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = com.example.ui.theme.TextAccent,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
                shape = CircleShape,
                modifier = Modifier
                    .background(com.example.ui.theme.AccentGradient, shape = CircleShape)
                    .shadow(16.dp, CircleShape, spotColor = com.example.ui.theme.MutedPurpleDark)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nová kategorie", tint = androidx.compose.ui.graphics.Color.White)
            }
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.premiumBackground()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                val catColor = category.colorHex.toColor(com.example.ui.theme.MutedPurple)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .premiumCardStyle(containerColor = com.example.ui.theme.CardSurfaceDark, shadowElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(catColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.iconName.toIcon(),
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = category.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = com.example.ui.theme.TextPrimary
                        )
                        IconButton(onClick = {
                            editingCategory = category
                            isEditorOpen = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Upravit", tint = com.example.ui.theme.TextSecondary)
                        }
                        IconButton(onClick = { categoryToDelete = category }) {
                            Icon(Icons.Default.Delete, contentDescription = "Smazat", tint = androidx.compose.ui.graphics.Color(0xFFFF4D4D))
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
                containerColor = com.example.ui.theme.PremiumBackgroundBottom,
                titleContentColor = com.example.ui.theme.TextPrimary,
                textContentColor = com.example.ui.theme.TextSecondary,
                title = { Text("Smazat kategorii?", fontWeight = FontWeight.Bold) },
                text = { Text("Opravdu chcete smazat kategorii '${categoryToDelete?.name}'? Všechny odkazy v této kategorii zůstanou uloženy, ale přeřadí se do 'Nezarazeno'.") },
                confirmButton = {
                    TextButton(onClick = {
                        categoryToDelete?.let { viewModel.deleteCategory(it) }
                        categoryToDelete = null
                    }) {
                        Text("Smazat", color = androidx.compose.ui.graphics.Color(0xFFFF4D4D))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToDelete = null }) {
                        Text("Zrušit", color = com.example.ui.theme.TextSecondary)
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
    var isAutoTaggingEnabled by remember { mutableStateOf(category.isAutoTaggingEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = com.example.ui.theme.PremiumBackgroundBottom,
        titleContentColor = com.example.ui.theme.TextPrimary,
        textContentColor = com.example.ui.theme.TextSecondary,
        title = { Text(if (category.id == 0) "Nová kategorie" else "Upravit", fontWeight = FontWeight.Bold) },
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
                IconPickerGrid(selectedIconName = iconName, onIconSelected = { iconName = it }, colorHex = colorHex)

                Text("Barva", style = MaterialTheme.typography.labelMedium)
                ColorPickerGrid(selectedColorHex = colorHex, onColorSelected = { colorHex = it })
                
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isAutoTaggingEnabled = !isAutoTaggingEnabled }.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-tagování", style = MaterialTheme.typography.bodyMedium, color = com.example.ui.theme.TextPrimary)
                        Text("Přiřadit tagy automaticky.", style = MaterialTheme.typography.bodySmall, color = com.example.ui.theme.TextSecondary)
                    }
                    Switch(
                        checked = isAutoTaggingEnabled,
                        onCheckedChange = { isAutoTaggingEnabled = it }
                    )
                }

                // Preview
                Surface(
                    color = colorHex.toColor().copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(iconName.toIcon(), contentDescription = null, tint = colorHex.toColor(), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = name.ifBlank { "Náhled" },
                            color = colorHex.toColor(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(category.copy(name = name.trim(), colorHex = colorHex, iconName = iconName, isAutoTaggingEnabled = isAutoTaggingEnabled)) },
                enabled = name.isNotBlank()
            ) {
                Text("Uložit", color = com.example.ui.theme.MutedPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit", color = com.example.ui.theme.TextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconPickerGrid(
    selectedIconName: String,
    onIconSelected: (String) -> Unit,
    colorHex: String
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        availableIcons.keys.take(16).forEach { name -> // Limit to 16 for UI space
            val isSelected = name == selectedIconName
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) colorHex.toColor().copy(alpha = 0.2f) else com.example.ui.theme.CardSurfaceDark)
                    .clickable { onIconSelected(name) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = name.toIcon(),
                    contentDescription = null,
                    tint = if (isSelected) colorHex.toColor() else com.example.ui.theme.TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

