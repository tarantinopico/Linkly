package com.example.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.LinkWithTagsAndCategory
import com.example.ui.screens.HomeViewModel
import com.example.ui.theme.*
import com.example.ui.utils.premiumCardStyle
import kotlinx.coroutines.delay

@Composable
fun DashboardTab(
    viewModel: HomeViewModel,
    totalLinksCount: Int,
    favoritesCount: Int,
    unreadCount: Int,
    categoriesCount: Int,
    recentLinks: List<LinkWithTagsAndCategory>,
    onNavigateToDetail: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp, start = 16.dp, end = 16.dp, top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text("Přehled", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Bookmarks, title = "Všechny", count = totalLinksCount, gradient = AllGradient, solidColor = Color(0xFF38EF7D), delayMillis = 0)
                PremiumStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Favorite, title = "Oblíbené", count = favoritesCount, gradient = FavoriteGradient, solidColor = Color(0xFFFF4D4D), delayMillis = 50)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.MarkEmailUnread, title = "Nepřečtené", count = unreadCount, gradient = UnreadGradient, solidColor = Color(0xFF0072FF), delayMillis = 100)
                PremiumStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Category, title = "Kategorie", count = categoriesCount, gradient = CategoryGradient, solidColor = Color(0xFF4568DC), delayMillis = 150)
            }
        }

        if (recentLinks.isNotEmpty()) {
            item {
                Text("Nedávno přidané", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(recentLinks) { linkItem ->
                LinkCard(
                    linkWithDetails = linkItem,
                    isSelected = false,
                    isSelectionMode = false,
                    onLongClick = { },
                    onClick = { onNavigateToDetail(linkItem.link.id) },
                    onToggleFavorite = { viewModel.toggleFavorite(linkItem.link) },
                    onToggleRead = { viewModel.toggleReadStatus(linkItem.link) }
                )
            }
        } 
        
        if (categoriesCount == 0) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp).premiumCardStyle(containerColor = CardSurfaceDark)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF4568DC).copy(alpha=0.15f)), contentAlignment=Alignment.Center) {
                            Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(32.dp), tint = MutedPurple)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Začněte tím pravým způsobem",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Můžeme vám předpřipravit nejpoužívanější kategorie pro snadnější organizaci.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.createSampleCategories() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(),
                            modifier = Modifier.background(AccentGradient, shape = RoundedCornerShape(12.dp))
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                                Text("Vytvořit kategorie", color = TextAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumStatCard(
    modifier: Modifier = Modifier, 
    icon: ImageVector, 
    title: String, 
    count: Int,
    gradient: Brush,
    solidColor: Color,
    delayMillis: Int
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        isVisible = true
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400), initialOffsetY = { 20 }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .clickable(interactionSource = interactionSource, indication = null) { /* Future action */ }
                .premiumCardStyle(containerColor = CardSurfaceDark, shadowElevation = 16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Colored Icon Circle
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(solidColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Small inner gradient mask or just solid tinted icon
                    Text(
                        // Just a visual hack for gradient icon
                        text = "",
                        style = androidx.compose.ui.text.TextStyle(brush = gradient)
                    )
                    Icon(icon, contentDescription = null, tint = solidColor, modifier = Modifier.size(24.dp))
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = count.toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontWeight = FontWeight.Medium)
            }
        }
    }
}
