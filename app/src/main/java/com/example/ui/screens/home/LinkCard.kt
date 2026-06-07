package com.example.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.entity.LinkWithTagsAndCategory
import com.example.ui.utils.toColor
import com.example.ui.utils.openUrl
import com.example.ui.utils.shimmerEffect
import com.example.LinklyApplication

import com.example.ui.utils.getRelativeTime
import java.net.URL

import com.example.ui.utils.premiumCardStyle

@Composable
fun LinkCard(
    linkWithDetails: LinkWithTagsAndCategory,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onToggleFavorite: () -> Unit,
    onToggleRead: () -> Unit,
    onDeleteClick: () -> Unit = {},
    onShowSnackbar: (String) -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val useInternalBrowser = (context.applicationContext as LinklyApplication).appSettings.useInternalBrowser.collectAsState(initial = true).value
    val haptic = LocalHapticFeedback.current

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) com.example.ui.theme.MutedPurple.copy(alpha = 0.2f) 
                      else com.example.ui.theme.CardSurfaceDark,
        animationSpec = tween(200)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .premiumCardStyle(containerColor = containerColor, shadowElevation = 12.dp)
            .pointerInput(isSelected) {
                detectTapGestures(
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    },
                    onTap = {
                        if (isSelectionMode) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onLongClick()
                        } else {
                            onClick()
                        }
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(com.example.ui.theme.CardSurfaceLight),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = linkWithDetails.link.imageUrl
                val faviconUrl = linkWithDetails.link.faviconUrl
                val imageToLoad = if (!imageUrl.isNullOrEmpty()) imageUrl else faviconUrl
                if (!imageToLoad.isNullOrEmpty()) {
                    SubcomposeAsyncImage(
                        model = imageToLoad,
                        contentDescription = "Link Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Error",
                                tint = com.example.ui.theme.TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Placeholder",
                        tint = com.example.ui.theme.TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!linkWithDetails.link.isRead) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(com.example.ui.theme.MutedPurple)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = linkWithDetails.link.title.takeIf { it.isNotEmpty() } ?: linkWithDetails.link.url,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (!linkWithDetails.link.isRead) FontWeight.Bold else FontWeight.Medium,
                        color = com.example.ui.theme.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    linkWithDetails.category?.let { category ->
                        val catColor = category.colorHex.toColor(com.example.ui.theme.MutedPurple)
                        Surface(
                            color = catColor.copy(alpha = 0.15f),
                            shape = CircleShape
                        ) {
                            Box(modifier = Modifier.size(8.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = catColor,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    
                    if (linkWithDetails.tags.isNotEmpty()) {
                        linkWithDetails.tags.take(2).forEach { tag ->
                            Text(
                                text = "#${tag.name}",
                                style = MaterialTheme.typography.labelMedium,
                                color = com.example.ui.theme.TextSecondary,
                                modifier = Modifier.padding(end = 6.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (linkWithDetails.tags.size > 2) {
                            Text(
                                text = "+${linkWithDetails.tags.size - 2}",
                                style = MaterialTheme.typography.labelMedium,
                                color = com.example.ui.theme.TextSecondary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = getRelativeTime(linkWithDetails.link.addedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.ui.theme.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = com.example.ui.theme.MutedPurple,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (!isSelectionMode) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(linkWithDetails.link.url))
                            onShowSnackbar("Zkopírováno")
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy Link",
                            tint = com.example.ui.theme.TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { expanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = com.example.ui.theme.TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(com.example.ui.theme.CardSurfaceLight)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Otevřít") },
                                onClick = {
                                    expanded = false
                                    openUrl(context, linkWithDetails.link.url, useInternalBrowser)
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
                                    Icon(if (linkWithDetails.link.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = if (linkWithDetails.link.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (linkWithDetails.link.isRead) "Označit jako nepřečtené" else "Označit jako přečtené") },
                                onClick = {
                                    expanded = false
                                    onToggleRead()
                                },
                                leadingIcon = { Icon(if (linkWithDetails.link.isRead) Icons.Default.MarkEmailUnread else Icons.Default.CheckCircleOutline, contentDescription = null) }
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
}
