package com.example.readingtracker.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.readingtracker.ui.navigation.AppNavigation
import com.example.readingtracker.ui.navigation.Routes
import com.example.readingtracker.ui.navigation.bottomNavItems

@Composable
fun ReadingTrackerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // フッターナビゲーションを非表示にする画面のリスト
    val routesWithoutBottomBar = listOf(
        Routes.BARCODE_SCANNER,
        Routes.BOOK_REGISTRATION_METHOD,
        Routes.BOOK_CONFIRMATION,
        Routes.PURPOSE_SETTING,
        Routes.BOOK_DETAIL,
        Routes.MEMO_ADD,
        Routes.ACTION_ITEM_CREATE
    )
    
    // 現在のルートがフッターナビゲーションを非表示にする画面かチェック
    val shouldShowBottomBar = currentDestination?.route?.let { currentRoute ->
        !routesWithoutBottomBar.any { route ->
            // パラメータ付きルートの場合は、ベースルートで判定
            if (route.contains("{")) {
                currentRoute.startsWith(route.substringBefore("{"))
            } else {
                currentRoute == route
            }
        }
    } ?: true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}