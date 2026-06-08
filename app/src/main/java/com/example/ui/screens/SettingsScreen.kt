package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.LinklyApplication
import com.example.data.repository.BackupRestoreManager
import kotlinx.coroutines.launch

import com.example.ui.utils.premiumBackground
import com.example.ui.utils.premiumCardStyle
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun SettingsIconContainer(icon: ImageVector, colorHex: Long) {
    val containerColor = androidx.compose.ui.graphics.Color(colorHex)
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(containerColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = containerColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToTags: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as LinklyApplication
    val backupRestoreManager = remember { BackupRestoreManager(context, application.repository) }
    
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(backupRestoreManager, application.repository)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        when (uiState) {
            is SettingsUiState.Success -> {
                snackbarHostState.showSnackbar((uiState as SettingsUiState.Success).message)
                viewModel.resetState()
            }
            is SettingsUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as SettingsUiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportData(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importData(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Nastavení", fontWeight = FontWeight.Bold) },
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
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.premiumBackground()
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                ListItem(
                    headlineContent = { Text("Správa kategorií", color = com.example.ui.theme.TextPrimary, fontWeight = FontWeight.Medium) },
                    leadingContent = { SettingsIconContainer(Icons.Default.Category, 0xFF4568DC) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp), tint = com.example.ui.theme.TextSecondary) },
                    modifier = Modifier.clickable { onNavigateToCategories() },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
                ListItem(
                    headlineContent = { Text("Správa tagů", color = com.example.ui.theme.TextPrimary, fontWeight = FontWeight.Medium) },
                    leadingContent = { SettingsIconContainer(Icons.Default.Label, 0xFF0072FF) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp), tint = com.example.ui.theme.TextSecondary) },
                    modifier = Modifier.clickable { onNavigateToTags() },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
                Text(
                    text = "Obecné",
                    style = MaterialTheme.typography.titleSmall,
                    color = com.example.ui.theme.MutedPurple,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                )

                val appSettings = application.appSettings
                val useInternalBrowser by appSettings.useInternalBrowser.collectAsStateWithLifecycle()
                val isAutoTaggingEnabled by appSettings.isAutoTaggingEnabled.collectAsStateWithLifecycle()
                var showAutoTaggingRules by remember { mutableStateOf(false) }

                ListItem(
                    headlineContent = { Text("Vestavěný prohlížeč", color = com.example.ui.theme.TextPrimary, fontWeight = FontWeight.Medium) },
                    supportingContent = { Text("Otevírat odkazy přímo v aplikaci", color = com.example.ui.theme.TextSecondary) },
                    leadingContent = { SettingsIconContainer(Icons.Default.Language, 0xFF38EF7D) },
                    trailingContent = {
                        Switch(
                            checked = useInternalBrowser,
                            onCheckedChange = { appSettings.setUseInternalBrowser(it) }
                        )
                    },
                    modifier = Modifier.clickable { appSettings.setUseInternalBrowser(!useInternalBrowser) },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )

                ListItem(
                    headlineContent = { Text("Automatické tagování", color = com.example.ui.theme.TextPrimary, fontWeight = FontWeight.Medium) },
                    supportingContent = { Text("Přiřadit tagy podle pravidel", color = com.example.ui.theme.TextSecondary) },
                    leadingContent = { SettingsIconContainer(Icons.Default.Label, 0xFFFF4D4D) },
                    trailingContent = {
                        Switch(
                            checked = isAutoTaggingEnabled,
                            onCheckedChange = { appSettings.setAutoTaggingEnabled(it) }
                        )
                    },
                    modifier = Modifier.clickable { appSettings.setAutoTaggingEnabled(!isAutoTaggingEnabled) },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )

                if (isAutoTaggingEnabled) {
                    ListItem(
                        headlineContent = { Text("Správa pravidel pro domény", color = com.example.ui.theme.TextPrimary) },
                        supportingContent = { Text("Nastavte weby a tagy.", color = com.example.ui.theme.TextSecondary) },
                        modifier = Modifier.clickable { showAutoTaggingRules = true }.padding(start = 56.dp),
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    )
                }

                if (showAutoTaggingRules) {
                    val rules by viewModel.allAutoTagRules.collectAsStateWithLifecycle()
                    var newDomain by remember { mutableStateOf("") }
                    var newTag by remember { mutableStateOf("") }
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                    ModalBottomSheet(
                        onDismissRequest = { showAutoTaggingRules = false },
                        sheetState = sheetState,
                        containerColor = com.example.ui.theme.PremiumBackgroundBottom,
                        dragHandle = { BottomSheetDefaults.DragHandle() }
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp).imePadding()) {
                            Text("Pravidla tagování", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = com.example.ui.theme.TextPrimary)
                            Spacer(Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newDomain,
                                    onValueChange = { newDomain = it },
                                    label = { Text("Doména") },
                                    modifier = Modifier.weight(1.5f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newTag,
                                    onValueChange = { newTag = it },
                                    label = { Text("Tag") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.addAutoTagRule(newDomain, newTag)
                                    newDomain = ""
                                    newTag = ""
                                },
                                modifier = Modifier.align(Alignment.End),
                                enabled = newDomain.isNotBlank() && newTag.isNotBlank()
                            ) {
                                Text("Přidat")
                            }

                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = com.example.ui.theme.CardBorder)
                            
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                            ) {
                                items(rules, key = { it.id }) { rule ->
                                    ListItem(
                                        headlineContent = { Text(rule.domain, fontWeight = FontWeight.SemiBold, color = com.example.ui.theme.TextPrimary) },
                                        supportingContent = { Text("Tag: ${rule.tagName}", color = com.example.ui.theme.MutedPurple) },
                                        trailingContent = {
                                            IconButton(onClick = { viewModel.deleteAutoTagRule(rule) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Smazat", tint = com.example.ui.theme.TextSecondary)
                                            }
                                        },
                                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Text(
                    text = "Vzhled",
                    style = MaterialTheme.typography.titleSmall,
                    color = com.example.ui.theme.MutedPurple,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                )
                
                // Color Picker Row
                val themeManager = application.themeManager
                val currentAccentColor by themeManager.accentColor.collectAsStateWithLifecycle()
                val colors = listOf(
                    androidx.compose.ui.graphics.Color(0xFF1E88E5), // Blue
                    androidx.compose.ui.graphics.Color(0xFF9D8DF0), // Purple
                    androidx.compose.ui.graphics.Color(0xFFF06292), // Pink
                    androidx.compose.ui.graphics.Color(0xFF43A047), // Green
                    androidx.compose.ui.graphics.Color(0xFFFF9800)  // Orange
                )
                
                ListItem(
                    headlineContent = { Text("Téma (Akcentní barva)", color = com.example.ui.theme.TextPrimary, fontWeight = FontWeight.Medium) },
                    supportingContent = { 
                        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            colors.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { themeManager.setAccentColor(color) }
                                ) {
                                    if (currentAccentColor == color) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Vybráno",
                                            tint = androidx.compose.ui.graphics.Color.White,
                                            modifier = Modifier.align(Alignment.Center).size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    leadingContent = { SettingsIconContainer(Icons.Default.ColorLens, 0xFF9D8DF0) },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )

                Text(
                    text = "Data a záloha",
                    style = MaterialTheme.typography.titleSmall,
                    color = com.example.ui.theme.MutedPurple,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                )
                ListItem(
                    headlineContent = { Text("Zálohovat data", color = com.example.ui.theme.TextPrimary, fontWeight = FontWeight.Medium) },
                    supportingContent = { Text("Exportovat do JSON souboru", color = com.example.ui.theme.TextSecondary) },
                    leadingContent = { SettingsIconContainer(Icons.Default.Backup, 0xFFF06292) },
                    modifier = Modifier.clickable { exportLauncher.launch("linkly_backup.json") },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
                ListItem(
                    headlineContent = { Text("Obnovit data ze zálohy", color = com.example.ui.theme.TextPrimary, fontWeight = FontWeight.Medium) },
                    supportingContent = { Text("Upozornění: aktuální data budou přepsána", color = androidx.compose.ui.graphics.Color(0xFFFF4D4D).copy(alpha=0.8f)) },
                    leadingContent = { SettingsIconContainer(Icons.Default.Restore, 0xFFFF9800) },
                    modifier = Modifier.clickable { importLauncher.launch(arrayOf("application/json")) },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )

                Text(
                    text = "O aplikaci",
                    style = MaterialTheme.typography.titleSmall,
                    color = com.example.ui.theme.MutedPurple,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                )
                ListItem(
                    headlineContent = { Text("Verze", color = com.example.ui.theme.TextPrimary, fontWeight = FontWeight.Medium) },
                    supportingContent = { Text("1.0.0 (Produkční sestavení)", color = com.example.ui.theme.TextSecondary) },
                    leadingContent = { SettingsIconContainer(Icons.Default.Info, 0xFF43A047) },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
            }

            if (uiState is SettingsUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CardSurfaceDark),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.padding(32.dp).premiumCardStyle(containerColor = com.example.ui.theme.CardSurfaceDark)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(color = com.example.ui.theme.MutedPurple)
                            Spacer(Modifier.height(16.dp))
                            Text((uiState as SettingsUiState.Loading).message, color = com.example.ui.theme.TextPrimary)
                        }
                    }
                }
            }
        }
    }
}
