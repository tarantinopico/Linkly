package com.example.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

val availableIcons = mapOf(
    "Folder" to Icons.Default.Folder,
    "Work" to Icons.Default.Work,
    "Restaurant" to Icons.Default.Restaurant,
    "Flight" to Icons.Default.Flight,
    "Music" to Icons.Default.MusicNote,
    "School" to Icons.Default.School,
    "Shopping" to Icons.Default.ShoppingCart,
    "Health" to Icons.Default.Favorite,
    "Tech" to Icons.Default.Computer,
    "Movie" to Icons.Default.Movie,
    "Book" to Icons.Default.Book,
    "Game" to Icons.Default.SportsEsports,
    "Car" to Icons.Default.DirectionsCar,
    "Home" to Icons.Default.Home,
    "Star" to Icons.Default.Star,
    "Account" to Icons.Default.Person,
    "Code" to Icons.Default.Code,
    "Money" to Icons.Default.AttachMoney,
    "Fitness" to Icons.Default.FitnessCenter,
    "Pets" to Icons.Default.Pets
)

fun String.toIcon(): ImageVector {
    return availableIcons[this] ?: Icons.Default.Folder
}
