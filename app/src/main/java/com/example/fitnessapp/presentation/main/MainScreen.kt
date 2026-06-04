package com.example.fitnessapp.presentation.main

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitnessapp.presentation.home.HomeScreen
import com.example.fitnessapp.presentation.mybookings.MyBookingsScreen
import com.example.fitnessapp.presentation.pass.QrPassScreen
import com.example.fitnessapp.presentation.profile.ProfileScreen

private sealed class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomTab("home", "Занятия", Icons.Default.CalendarMonth)
    object MyBookings : BottomTab(
        "my_bookings",
        "Мои записи",
        Icons.Default.BookmarkAdded
    )

    object Pass : BottomTab("pass", "Пропуск", Icons.Default.QrCode2)

    object Profile : BottomTab("profile", "Настройки", Icons.Default.Settings)
}

private val tabs = listOf(BottomTab.Home, BottomTab.MyBookings, BottomTab.Pass, BottomTab.Profile)

@Composable
fun MainScreen(onLogout: () -> Unit, onBookingClick: (Long) -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any {
                            it.route == tab.route
                        } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Home.route,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(BottomTab.Home.route) { HomeScreen(onBookingClick = onBookingClick) }
            composable(BottomTab.MyBookings.route) {
                MyBookingsScreen(
                    onBookingClick = onBookingClick
                )
            }
            composable(BottomTab.Pass.route) { QrPassScreen() }
            composable(BottomTab.Profile.route) { ProfileScreen(onLogout = onLogout) }
        }
    }
}
