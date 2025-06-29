package com.example.readingtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.readingtracker.ui.screens.home.HomeScreen
import com.example.readingtracker.ui.screens.statistics.StatisticsScreen
import com.example.readingtracker.ui.screens.bookregistration.*
import com.example.readingtracker.ui.screens.bookdetail.BookDetailScreen
import com.example.readingtracker.ui.screens.memo.MemoAddScreen
import com.example.readingtracker.ui.screens.actionitem.ActionItemCreateScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(BottomNavItem.Statistics.route) {
            StatisticsScreen(navController = navController)
        }
        
        composable(Routes.BOOK_REGISTRATION_METHOD) {
            BookRegistrationMethodScreen(navController = navController)
        }
        
        composable(Routes.BARCODE_SCANNER) {
            BarcodeScannerScreen(navController = navController)
        }
        
        composable(Routes.BOOK_CONFIRMATION) {
            BookConfirmationScreen(navController = navController)
        }
        
        composable(Routes.PURPOSE_SETTING) {
            PurposeSettingScreen(navController = navController)
        }
        
        composable(
            route = Routes.BOOK_DETAIL,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookDetailScreen(navController = navController, bookId = bookId)
        }
        
        composable(
            route = Routes.MEMO_ADD,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            MemoAddScreen(navController = navController, bookId = bookId)
        }
        
        composable(
            route = Routes.ACTION_ITEM_CREATE,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("memoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            val memoId = backStackEntry.arguments?.getString("memoId") ?: ""
            ActionItemCreateScreen(navController = navController, bookId = bookId, memoId = memoId)
        }
    }
}