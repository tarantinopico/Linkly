package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.LinklyApplication
import com.example.data.local.entity.Link
import com.example.ui.screens.home.CategoriesTab
import com.example.ui.screens.home.DashboardTab
import com.example.ui.screens.home.LinksTab

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddEdit: (Int?) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val application = LocalContext.current.applicationContext as LinklyApplication
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(application.repository, application.appSettings))

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val categoriesWithCount by viewModel.categoriesWithCount.collectAsStateWithLifecycle()
    val links by viewModel.links.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val showUnreadOnly by viewModel.showUnreadOnly.collectAsStateWithLifecycle()
    
    val totalLinksCount by viewModel.totalLinksCount.collectAsStateWithLifecycle()
    val favoritesCount by viewModel.favoritesCount.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()
    val recentLinks by viewModel.recentLinks.collectAsStateWithLifecycle()
    
    val selectedLinkIds by viewModel.selectedLinkIds.collectAsStateWithLifecycle()

    var currentTab by remember { mutableIntStateOf(0) }
    var isSearchActive by remember { mutableStateOf(false) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showQuickAddSheet by remember { mutableStateOf(false) }
    val quickAddSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var quickAddUrl by remember { mutableStateOf("") }
    var quickAddIsLoading by remember { mutableStateOf(false) }
    var quickAddTitle by remember { mutableStateOf<String?>(null) }
    var quickAddError by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val clipboardUrl by viewModel.clipboardUrl.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (clipboardManager.hasText()) {
                    val text = clipboardManager.getText()?.text
                    if (text != null && (text.startsWith("http://") || text.startsWith("https://"))) {
                        viewModel.checkClipboardUrl(text)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (selectedLinkIds.isNotEmpty() && currentTab == 1) {
                TopAppBar(
                    title = { Text("${selectedLinkIds.size} vybráno") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Zrušit výběr")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.markSelectedAsRead(true) }) {
                            Icon(Icons.Default.Checklist, contentDescription = "Přečtené")
                        }
                        IconButton(onClick = { viewModel.deleteSelectedLinks() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Smazat")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            } else if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Hledat...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            isSearchActive = false
                            viewModel.onSearchQueryChanged("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Zavřít hledání")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            } else {
                TopAppBar(
                    title = { 
                        Text(
                            when(currentTab) {
                                0 -> "Přehled"
                                1 -> "Odkazy"
                                2 -> "Kategorie"
                                else -> "Linkly"
                            }, 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    actions = {
                        if (currentTab == 1) {
                            IconButton(onClick = { viewModel.onShowUnreadOnlyChanged(!showUnreadOnly) }) {
                                Icon(
                                    if (showUnreadOnly) Icons.Default.MarkEmailRead else Icons.Default.MarkEmailUnread, 
                                    contentDescription = "Filtr nepřečtených",
                                    tint = if (showUnreadOnly) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                )
                            }
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                            Box {
                                IconButton(onClick = { isSortMenuExpanded = true }) {
                                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                                }
                                DropdownMenu(
                                    expanded = isSortMenuExpanded,
                                    onDismissRequest = { isSortMenuExpanded = false }
                                ) {
                                    SortOrder.values().forEach { order ->
                                        DropdownMenuItem(
                                            text = { Text(order.displayName) },
                                            onClick = {
                                                viewModel.onSortOrderChanged(order)
                                                isSortMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Přehled") }
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Odkazy") }
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Category, contentDescription = null) },
                    label = { Text("Kategorie") }
                )
            }
        },
        floatingActionButton = {
            if (currentTab != 2) {
                FloatingActionButton(
                    onClick = { showQuickAddSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Link")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = clipboardUrl != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    clipboardUrl?.let { url ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Přidat zkopírovaný odkaz?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Text(url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Row {
                                    TextButton(onClick = { viewModel.dismissClipboardUrl() }) {
                                        Text("Ne", color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.dismissClipboardUrl()
                                            // Handle quick add or navigate to AddEdit
                                            onNavigateToAddEdit(null) // Better to open sheet
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Přidat")
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300))).togetherWith(fadeOut(animationSpec = tween(300)))
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> DashboardTab(
                        viewModel = viewModel,
                        totalLinksCount = totalLinksCount,
                        favoritesCount = favoritesCount,
                        unreadCount = unreadCount,
                        categoriesCount = categoriesWithCount.size,
                        recentLinks = recentLinks,
                        onNavigateToDetail = onNavigateToDetail
                    )
                    1 -> Column {
                        ScrollableTabRow(
                            selectedTabIndex = if (selectedCategoryId == null) 0 else categories.indexOfFirst { it.id == selectedCategoryId } + 1,
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground,
                            edgePadding = 16.dp,
                            divider = {}
                        ) {
                            Tab(
                                selected = selectedCategoryId == null,
                                onClick = { viewModel.onCategorySelected(null) },
                                text = { Text("Vše") }
                            )
                            categories.forEach { category ->
                                Tab(
                                    selected = selectedCategoryId == category.id,
                                    onClick = { viewModel.onCategorySelected(category.id) },
                                    text = { Text(category.name) }
                                )
                            }
                        }
                        LinksTab(
                            viewModel = viewModel,
                            links = links,
                            selectedLinkIds = selectedLinkIds,
                            onNavigateToDetail = onNavigateToDetail,
                            onNavigateToAddEdit = onNavigateToAddEdit,
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onDeleteClick = { 
                                viewModel.deleteLink(it)
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Odkaz smazán",
                                        actionLabel = "Zpět"
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoDelete()
                                    }
                                }
                            },
                            onShowSnackbar = { msg -> coroutineScope.launch { snackbarHostState.showSnackbar(msg) } }
                        )
                    }
                    2 -> CategoriesTab(
                        viewModel = viewModel,
                        categoriesWithCount = categoriesWithCount,
                        onCategoryClick = { categoryId ->
                            viewModel.onCategorySelected(categoryId)
                            currentTab = 1
                        }
                    )
                }
            }
            } // Close Column
        }
        if (showQuickAddSheet) {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(showQuickAddSheet) {
                if (showQuickAddSheet) {
                    delay(300) // wait for bottom sheet entry animation
                    focusRequester.requestFocus()
                }
            }

            ModalBottomSheet(
                onDismissRequest = { 
                    showQuickAddSheet = false 
                    quickAddUrl = ""
                    quickAddError = null
                },
                sheetState = quickAddSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Rychlé přidání odkazu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = quickAddUrl,
                        onValueChange = { quickAddUrl = it; quickAddError = null },
                        label = { Text("URL adresa") },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        singleLine = true,
                        isError = quickAddError != null,
                        supportingText = { quickAddError?.let { Text(it) } },
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { 
                            showQuickAddSheet = false
                            onNavigateToAddEdit(null) 
                        }) {
                            Text("Více možností")
                        }

                        Button(
                            onClick = {
                                if (quickAddUrl.isBlank()) {
                                    quickAddError = "Zadejte URL."
                                    return@Button
                                }
                                quickAddIsLoading = true
                                keyboardController?.hide()
                                viewModel.quickAddLink(quickAddUrl) { success, msg ->
                                    quickAddIsLoading = false
                                    if (success) {
                                        showQuickAddSheet = false
                                        quickAddUrl = ""
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Odkaz přidán") }
                                    } else {
                                        quickAddError = msg
                                    }
                                }
                            },
                            enabled = !quickAddIsLoading
                        ) {
                            if (quickAddIsLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Uložit")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

