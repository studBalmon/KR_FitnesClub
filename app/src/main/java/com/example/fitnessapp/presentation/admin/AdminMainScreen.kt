package com.example.fitnessapp.presentation.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitnessapp.presentation.profile.ProfileScreen

private sealed class AdminTab(val route: String, val label: String, val icon: ImageVector) {
    object Bookings : AdminTab(
        "admin_bookings",
        "Занятия",
        Icons.Default.CalendarMonth
    )

    object Users : AdminTab(
        "admin_users",
        "Пользователи",
        Icons.Default.Group
    )

    object Catalogs : AdminTab(
        "admin_catalogs",
        "Справочники",
        Icons.Default.FitnessCenter
    )

    object Profile : AdminTab(
        "admin_profile",
        "Настройки",
        Icons.Default.Settings
    )
}

private val adminTabs =
    listOf(AdminTab.Bookings, AdminTab.Users, AdminTab.Catalogs, AdminTab.Profile)

@Composable
fun AdminMainScreen(
    onLogout: () -> Unit,
    onEditBooking: (Long) -> Unit,
    onViewParticipants: (Long) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                adminTabs.forEach { tab ->
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
            startDestination = AdminTab.Bookings.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AdminTab.Bookings.route) {
                AdminBookingsScreen(
                    onEditBooking = onEditBooking,
                    onViewParticipants = onViewParticipants
                )
            }
            composable(AdminTab.Users.route) {
                AdminUsersScreen()
            }
            composable(AdminTab.Catalogs.route) {
                AdminCatalogsScreen()
            }
            composable(AdminTab.Profile.route) {
                ProfileScreen(onLogout = onLogout)
            }
        }
    }
}
