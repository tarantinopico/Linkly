package com.example.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.LinkWithTagsAndCategory
import com.example.ui.screens.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinksTab(
    viewModel: HomeViewModel,
    links: List<LinkWithTagsAndCategory>,
    selectedLinkIds: Set<Int>,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToAddEdit: (Int?) -> Unit,
    onToggleFavorite: (com.example.data.local.entity.Link) -> Unit,
    onDeleteClick: (com.example.data.local.entity.Link) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    if (links.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp), 
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(com.example.ui.theme.CardSurfaceLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Bookmarks,
                    contentDescription = null,
                    tint = com.example.ui.theme.TextSecondary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Zatím tu nic není",
                style = MaterialTheme.typography.titleLarge,
                color = com.example.ui.theme.TextPrimary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Přidejte si zajímavé odkazy pomocí tlačítka +",
                style = MaterialTheme.typography.bodyMedium,
                color = com.example.ui.theme.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(links, key = { it.link.id }) { linkWithDetails ->
                val dismissState = rememberSwipeToDismissBoxState(
                    positionalThreshold = { it * 0.4f },
                    confirmValueChange = { dismissValue ->
                        when (dismissValue) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                onNavigateToAddEdit(linkWithDetails.link.id)
                                false
                            }
                            SwipeToDismissBoxValue.EndToStart -> {
                                onDeleteClick(linkWithDetails.link)
                                false
                            }
                            else -> false
                        }
                    }
                )

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
                            isSelected = selectedLinkIds.contains(linkWithDetails.link.id),
                            isSelectionMode = selectedLinkIds.isNotEmpty(),
                            onLongClick = { viewModel.toggleSelection(linkWithDetails.link.id) },
                            onClick = { onNavigateToDetail(linkWithDetails.link.id) },
                            onEditClick = { onNavigateToAddEdit(linkWithDetails.link.id) },
                            onToggleFavorite = { onToggleFavorite(linkWithDetails.link) },
                            onToggleRead = { viewModel.toggleReadStatus(linkWithDetails.link) },
                            onDeleteClick = { onDeleteClick(linkWithDetails.link) },
                            onShowSnackbar = onShowSnackbar
                        )
                    }
                )
            }
        }
    }
}
