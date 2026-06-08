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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.LinklyApplication
import com.example.data.local.entity.Tag
import com.example.ui.utils.toColor
import com.example.ui.utils.premiumBackground
import com.example.ui.utils.premiumCardStyle

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
                title = { Text("Správa tagů", fontWeight = FontWeight.Bold) },
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
                    editingTag = Tag(name = "", colorHex = premiumColorsList.first())
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
                Icon(Icons.Default.Add, contentDescription = "Nový tag", tint = androidx.compose.ui.graphics.Color.White)
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
            items(tags, key = { it.id }) { tag ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .premiumCardStyle(containerColor = com.example.ui.theme.CardSurfaceDark, shadowElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(tag.colorHex?.toColor() ?: com.example.ui.theme.MutedPurple)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = tag.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = com.example.ui.theme.TextPrimary
                        )
                        IconButton(onClick = {
                            editingTag = tag
                            isEditorOpen = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Upravit", tint = com.example.ui.theme.TextSecondary)
                        }
                        IconButton(onClick = { tagToDelete = tag }) {
                            Icon(Icons.Default.Delete, contentDescription = "Smazat", tint = androidx.compose.ui.graphics.Color(0xFFFF4D4D))
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
                containerColor = com.example.ui.theme.PremiumBackgroundBottom,
                titleContentColor = com.example.ui.theme.TextPrimary,
                textContentColor = com.example.ui.theme.TextSecondary,
                title = { Text("Smazat tag?", fontWeight = FontWeight.Bold) },
                text = { Text("Opravdu chcete smazat tag '${tagToDelete?.name}'? Odkazy s tímto tagem zůstanou zachovány, pouze přijdou o tento tag.") },
                confirmButton = {
                    TextButton(onClick = {
                        tagToDelete?.let { viewModel.deleteTag(it) }
                        tagToDelete = null
                    }) {
                        Text("Smazat", color = androidx.compose.ui.graphics.Color(0xFFFF4D4D))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { tagToDelete = null }) {
                        Text("Zrušit", color = com.example.ui.theme.TextSecondary)
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
        containerColor = com.example.ui.theme.PremiumBackgroundBottom,
        titleContentColor = com.example.ui.theme.TextPrimary,
        textContentColor = com.example.ui.theme.TextSecondary,
        title = { Text(if (tag.id == 0) "Nový tag" else "Upravit tag", fontWeight = FontWeight.Bold) },
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
                
                // Preview
                Surface(
                    color = colorHex.toColor().copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = name.ifBlank { "Náhled tagu" },
                        color = colorHex.toColor(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(tag.copy(name = name.trim(), colorHex = colorHex)) },
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
                    .size(40.dp)
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
                            .background(androidx.compose.ui.graphics.Color.White)
                    )
                }
            }
        }
    }
}

