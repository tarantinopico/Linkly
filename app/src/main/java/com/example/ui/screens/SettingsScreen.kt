package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.repository.BackupRestoreManager
import kotlinx.coroutines.launch

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
        factory = SettingsViewModel.Factory(backupRestoreManager)
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
                title = { Text("Nastavení") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ListItem(
                    headlineContent = { Text("Správa kategorií") },
                    leadingContent = { Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.clickable { onNavigateToCategories() },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Správa tagů") },
                    leadingContent = { Icon(Icons.Default.Label, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.clickable { onNavigateToTags() },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
                Text(
                    text = "Obecné",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                )

                val appSettings = application.appSettings
                val useInternalBrowser by appSettings.useInternalBrowser.collectAsStateWithLifecycle()

                ListItem(
                    headlineContent = { Text("Vestavěný prohlížeč") },
                    supportingContent = { Text("Otevírat odkazy přímo v aplikaci") },
                    leadingContent = { Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingContent = {
                        Switch(
                            checked = useInternalBrowser,
                            onCheckedChange = { appSettings.setUseInternalBrowser(it) }
                        )
                    },
                    modifier = Modifier.clickable { appSettings.setUseInternalBrowser(!useInternalBrowser) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )

                HorizontalDivider()
                
                Text(
                    text = "Vzhled",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
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
                    headlineContent = { Text("Téma (Akcentní barva)") },
                    supportingContent = { 
                        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            colors.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
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
                    leadingContent = { Icon(Icons.Default.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
                HorizontalDivider()

                Text(
                    text = "Data a záloha",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                )
                ListItem(
                    headlineContent = { Text("Zálohovat data") },
                    supportingContent = { Text("Exportovat do JSON souboru") },
                    leadingContent = { Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.clickable { exportLauncher.launch("linkly_backup.json") },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Obnovit data ze zálohy") },
                    supportingContent = { Text("Upozornění: aktuální data budou přepsána") },
                    leadingContent = { Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { importLauncher.launch(arrayOf("application/json")) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
                HorizontalDivider()

                Text(
                    text = "O aplikaci",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                )
                ListItem(
                    headlineContent = { Text("Verze") },
                    supportingContent = { Text("1.0.0 (Produkční sestavení)") },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
            }

            if (uiState is SettingsUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(16.dp))
                            Text((uiState as SettingsUiState.Loading).message)
                        }
                    }
                }
            }
        }
    }
}
