package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.LinklyApplication
import com.example.data.local.entity.Link
import com.example.data.local.entity.LinkWithTagsAndCategory
import com.example.ui.utils.toColor
import com.example.ui.utils.toIcon

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
    val links by viewModel.links.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

    var isSearchActive by remember { mutableStateOf(false) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var linkToDelete by remember { mutableStateOf<Link?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (isSearchActive) {
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
                    title = { Text("Linkly", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    actions = {
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
                                        },
                                        trailingIcon = if (sortOrder == order) {
                                            { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(0.dp)) } // Just to align
                                        } else null
                                    )
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddEdit(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Link")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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

            if (links.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Žádné odkazy",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Klikněte na + pro přidání prvního odkazu.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(links, key = { it.link.id }) { linkWithDetails ->
                        var isVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(linkWithDetails.link.id) {
                            isVisible = true
                        }
                        
                        val dismissState = rememberSwipeToDismissBoxState(
                            positionalThreshold = { it * 0.4f },
                            confirmValueChange = { dismissValue ->
                                when (dismissValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        onNavigateToAddEdit(linkWithDetails.link.id)
                                        false
                                    }
                                    SwipeToDismissBoxValue.EndToStart -> {
                                        linkToDelete = linkWithDetails.link
                                        false
                                    }
                                    else -> false
                                }
                            }
                        )

                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 }
                        ) {
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color = when (dismissState.dismissDirection) {
                                        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                        else -> Color.Transparent
                                    }
                                    val alignment = when (dismissState.dismissDirection) {
                                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                        else -> Alignment.Center
                                    }
                                    val icon = when (dismissState.dismissDirection) {
                                        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                                        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                        else -> null
                                    }
                                    val iconColor = when (dismissState.dismissDirection) {
                                        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onPrimaryContainer
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
                                        else -> Color.Transparent
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(color)
                                            .padding(horizontal = 24.dp),
                                        contentAlignment = alignment
                                    ) {
                                        if (icon != null) {
                                            Icon(icon, contentDescription = null, tint = iconColor)
                                        }
                                    }
                                },
                                content = {
                                    LinkCard(
                                        linkWithDetails = linkWithDetails,
                                        onEditClick = { onNavigateToAddEdit(linkWithDetails.link.id) },
                                        onClick = { onNavigateToDetail(linkWithDetails.link.id) },
                                        onToggleFavorite = { viewModel.toggleFavorite(linkWithDetails.link) },
                                        onDeleteClick = { linkToDelete = linkWithDetails.link },
                                        onShowSnackbar = { msg -> coroutineScope.launch { snackbarHostState.showSnackbar(msg) } }
                                    )
                                }
                            )
                        }
                    }
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

@Composable
fun LinkCard(
    linkWithDetails: LinkWithTagsAndCategory,
    onEditClick: () -> Unit,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteClick: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = linkWithDetails.link.imageUrl
                val faviconUrl = linkWithDetails.link.faviconUrl
                val imageToLoad = if (!imageUrl.isNullOrEmpty()) imageUrl else faviconUrl
                if (!imageToLoad.isNullOrEmpty()) {
                    coil.compose.SubcomposeAsyncImage(
                        model = imageToLoad,
                        contentDescription = "Link Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Placeholder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = linkWithDetails.link.title.takeIf { it.isNotEmpty() } ?: linkWithDetails.link.url,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    linkWithDetails.category?.let { category ->
                        Surface(
                            color = category.colorHex.toColor(MaterialTheme.colorScheme.primary).copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Box(modifier = Modifier.size(10.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = category.colorHex.toColor(MaterialTheme.colorScheme.primary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    if (linkWithDetails.tags.isNotEmpty()) {
                        linkWithDetails.tags.take(2).forEach { tag ->
                            Text(
                                text = "#${tag.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (linkWithDetails.tags.size > 2) {
                            Text(
                                text = "+${linkWithDetails.tags.size - 2}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(linkWithDetails.link.url))
                        onShowSnackbar("Zkopírováno")
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy Link",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Otevřít") },
                            onClick = {
                                expanded = false
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkWithDetails.link.url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    onShowSnackbar("Nelze otevřít odkaz.")
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.OpenInBrowser, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (linkWithDetails.link.isFavorite) "Odebrat z oblíbených" else "Přidat do oblíbených") },
                            onClick = {
                                expanded = false
                                onToggleFavorite()
                            },
                            leadingIcon = {
                                Icon(
                                    if (linkWithDetails.link.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (linkWithDetails.link.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Upravit") },
                            onClick = {
                                expanded = false
                                onEditClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Smazat") },
                            onClick = {
                                expanded = false
                                onDeleteClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
        }
    }
}

