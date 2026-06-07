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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddEdit: (Int?) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val application = LocalContext.current.applicationContext as LinklyApplication
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(application.repository))

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
    var linkToDelete by remember { mutableStateOf<Link?>(null) }

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
                    onClick = { onNavigateToAddEdit(null) },
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
                            onDeleteClick = { linkToDelete = it },
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
        }
        
        if (linkToDelete != null) {
            AlertDialog(
                onDismissRequest = { linkToDelete = null },
                title = { Text("Smazat odkaz?") },
                text = { Text("Opravdu chcete tento odkaz trvale smazat?") },
                confirmButton = {
                    TextButton(onClick = {
                        linkToDelete?.let { viewModel.deleteLink(it) }
                        linkToDelete = null
                    }) {
                        Text("Smazat", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { linkToDelete = null }) {
                        Text("Zrušit")
                    }
                }
            )
        }
    }
}

