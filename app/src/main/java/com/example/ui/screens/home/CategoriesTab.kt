package com.example.ui.screens.home

// CategoriesTab.kt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Category
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.dao.CategoryWithCount
import com.example.ui.screens.HomeViewModel
import com.example.ui.utils.toColor
import com.example.ui.utils.toIcon

import com.example.ui.utils.premiumCardStyle

@Composable
fun CategoriesTab(
    viewModel: HomeViewModel,
    categoriesWithCount: List<CategoryWithCount>,
    onCategoryClick: (Int) -> Unit
) {
    if (categoriesWithCount.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp), 
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(com.example.ui.theme.CardSurfaceLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.Category,
                    contentDescription = null,
                    tint = com.example.ui.theme.TextSecondary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Žádné kategorie",
                style = MaterialTheme.typography.titleLarge,
                color = com.example.ui.theme.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Kategorie můžete vytvořit v Nastavení nebo přes widget na Přehledu.",
                style = MaterialTheme.typography.bodyMedium,
                color = com.example.ui.theme.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp, start = 16.dp, end = 16.dp, top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categoriesWithCount) { item ->
                val catColor = item.category.colorHex.toColor(com.example.ui.theme.MutedPurple)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .premiumCardStyle(containerColor = com.example.ui.theme.CardSurfaceDark)
                        .clickable { onCategoryClick(item.category.id) }
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(catColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.category.iconName.toIcon(),
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = item.category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${item.linkCount} odkazů",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.example.ui.theme.TextSecondary
                        )
                    }
                }
            }
        }
    }
}
