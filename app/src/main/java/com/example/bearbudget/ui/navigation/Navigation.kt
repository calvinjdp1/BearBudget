package com.example.bearbudget.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bearbudget.ui.screens.TransactionsScreen
import com.example.bearbudget.ui.screens.AddTransactionScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "transactions",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("transactions") { TransactionsScreen() }
            composable("summary") { Text("Summary Page") }
            composable("accounts") { Text("Accounts Page") }
            composable("add_transaction") {
                AddTransactionScreen(
                    onTransactionAdded = {
                        navController.navigate("transactions") {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: androidx.navigation.NavHostController) {
    val items = listOf(
        NavigationItem("transactions", Icons.Filled.List, "Transactions"),
        NavigationItem("add_transaction", Icons.Filled.Add, "Add"),
        NavigationItem("summary", Icons.Filled.Assessment, "Summary"),
        NavigationItem("accounts", Icons.Filled.AccountBalance, "Accounts")
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

data class NavigationItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
