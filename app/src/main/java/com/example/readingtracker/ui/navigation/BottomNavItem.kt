package com.example.readingtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem(
        route = "home",
        icon = Icons.Default.Home,
        label = "本棚"
    )
    
    object Statistics : BottomNavItem(
        route = "statistics",
        icon = Icons.Default.Build,
        label = "統計"
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Statistics
)