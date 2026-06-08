package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.LinklyApplication
import com.example.ui.utils.toColor
import com.example.ui.utils.toIcon
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.ui.utils.openUrl
import androidx.compose.runtime.collectAsState
import com.example.ui.utils.premiumBackground
import com.example.ui.utils.premiumCardStyle

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LinkDetailScreen(
    linkId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit
) {
    val application = LocalContext.current.applicationContext as LinklyApplication
    val useInternalBrowser = application.appSettings.useInternalBrowser.collectAsState(initial = true).value
    val viewModel: LinkDetailViewModel = viewModel(
        factory = LinkDetailViewModel.Factory(application.repository, linkId)
    )

    val linkDetails by viewModel.linkDetails.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detail odkazu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                    }
                },
                actions = {
                    linkDetails?.let { detail ->
                        IconButton(onClick = { viewModel.toggleFavorite(detail.link) }) {
                            Icon(
                                imageVector = if (detail.link.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Oblíbené",
                                tint = if (detail.link.isFavorite) androidx.compose.ui.graphics.Color(0xFFFF4D4D) else com.example.ui.theme.TextPrimary
                            )
                        }
                        IconButton(onClick = { onNavigateToEdit(detail.link.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Upravit")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Smazat")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = com.example.ui.theme.TextPrimary,
                    navigationIconContentColor = com.example.ui.theme.TextPrimary,
                    actionIconContentColor = com.example.ui.theme.TextPrimary
                )
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.premiumBackground()
    ) { paddingValues ->
        if (linkDetails == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = com.example.ui.theme.MutedPurple)
            }
        } else {
            val detail = linkDetails!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                if (!detail.link.imageUrl.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                        AsyncImage(
                            model = detail.link.imageUrl,
                            contentDescription = "Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(modifier = Modifier.fillMaxSize().background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(androidx.compose.ui.graphics.Color.Transparent, com.example.ui.theme.PremiumBackgroundTop),
                                startY = 100f
                            )
                        ))
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    detail.category?.let { category ->
                        Surface(
                            color = category.colorHex.toColor(com.example.ui.theme.MutedPurple).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = category.iconName.toIcon(),
                                    contentDescription = null,
                                    tint = category.colorHex.toColor(com.example.ui.theme.MutedPurple),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = category.name,
                                    color = category.colorHex.toColor(com.example.ui.theme.MutedPurple),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = detail.link.title.takeIf { it.isNotBlank() } ?: detail.link.url,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.TextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = detail.link.url,
                        style = MaterialTheme.typography.bodyLarge,
                        color = com.example.ui.theme.MutedPurple,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                openUrl(context, detail.link.url, useInternalBrowser)
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.MutedPurple)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Otevřít", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(detail.link.url))
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Zkopírováno do schránky")
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.CardBorder)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kopírovat", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (detail.tags.isNotEmpty()) {
                        Text("Tagy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.TextPrimary)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
                        ) {
                            detail.tags.forEach { tag ->
                                Surface(
                                    color = tag.colorHex?.toColor(com.example.ui.theme.CardSurfaceDark)?.copy(alpha = 0.2f) ?: com.example.ui.theme.CardSurfaceDark,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, tag.colorHex?.toColor(com.example.ui.theme.CardBorder)?.copy(alpha=0.3f) ?: com.example.ui.theme.CardBorder)
                                ) {
                                    Text(
                                        text = "#${tag.name}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = tag.colorHex?.toColor(com.example.ui.theme.TextSecondary) ?: com.example.ui.theme.TextSecondary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    if (detail.link.notes.isNotBlank()) {
                        Text("Poznámka", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.TextPrimary)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 24.dp)
                                .premiumCardStyle(containerColor = com.example.ui.theme.CardSurfaceLight, shadowElevation = 0.dp)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = detail.link.notes,
                                style = MaterialTheme.typography.bodyLarge,
                                color = com.example.ui.theme.TextPrimary
                            )
                        }
                    }

                    val dateString = SimpleDateFormat("d. M. yyyy HH:mm", Locale.getDefault()).format(Date(detail.link.addedAt))
                    Text(
                        text = "Přidáno: $dateString",
                        style = MaterialTheme.typography.labelMedium,
                        color = com.example.ui.theme.TextSecondary
                    )
                }
            }
        }

        if (showDeleteDialog && linkDetails != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = com.example.ui.theme.PremiumBackgroundBottom,
                titleContentColor = com.example.ui.theme.TextPrimary,
                textContentColor = com.example.ui.theme.TextSecondary,
                title = { Text("Smazat odkaz?", fontWeight = FontWeight.Bold) },
                text = { Text("Opravdu chcete tento odkaz trvale smazat?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteLink(linkDetails!!.link)
                        showDeleteDialog = false
                        onNavigateBack()
                    }) {
                        Text("Smazat", color = androidx.compose.ui.graphics.Color(0xFFFF4D4D), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Zrušit", color = com.example.ui.theme.TextSecondary)
                    }
                }
            )
        }
    }
}
