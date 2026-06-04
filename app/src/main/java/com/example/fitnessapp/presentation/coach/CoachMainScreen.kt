package com.example.fitnessapp.presentation.coach

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.QrCode2
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
import com.example.fitnessapp.presentation.pass.QrPassScreen
import com.example.fitnessapp.presentation.profile.ProfileScreen

private sealed class CoachTab(val route: String, val label: String, val icon: ImageVector) {
    object Bookings : CoachTab(
        "coach_bookings",
        "Мои занятия",
        Icons.Default.CalendarMonth
    )

    object Pass : CoachTab(
        "coach_pass",
        "Пропуск",
        Icons.Default.QrCode2
    )

    object Profile : CoachTab(
        "coach_profile",
        "Настройки",
        Icons.Default.Settings
    )
}

private val coachTabs = listOf(CoachTab.Bookings, CoachTab.Pass, CoachTab.Profile)

@Composable
fun CoachMainScreen(
    onLogout: () -> Unit,
    onCreateBooking: () -> Unit,
    onEditBooking: (Long) -> Unit,
    onViewParticipants: (Long) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                coachTabs.forEach { tab ->
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
        },
        floatingActionButton = {
            if (currentRoute == CoachTab.Bookings.route) {
                FloatingActionButton(onClick = onCreateBooking) {
                    Icon(Icons.Default.Add, contentDescription = "Создать занятие")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CoachTab.Bookings.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(CoachTab.Bookings.route) {
                CoachBookingsScreen(
                    onEditBooking = onEditBooking,
                    onViewParticipants = onViewParticipants
                )
            }
            composable(CoachTab.Pass.route) {
                QrPassScreen()
            }
            composable(CoachTab.Profile.route) {
                ProfileScreen(onLogout = onLogout)
            }
        }
    }
}
