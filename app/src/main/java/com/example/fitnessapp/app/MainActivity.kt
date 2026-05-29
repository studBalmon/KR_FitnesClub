package com.example.fitnessapp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.fitnessapp.data.local.ThemeDataStore
import com.example.fitnessapp.domain.repository.AuthRepository
import com.example.fitnessapp.presentation.auth.LoginScreen
import com.example.fitnessapp.presentation.auth.RegisterScreen
import com.example.fitnessapp.presentation.booking.BookingDetailScreen
import com.example.fitnessapp.presentation.main.MainScreen
import com.example.fitnessapp.presentation.navigation.Routes
import com.example.fitnessapp.presentation.theme.FitnessAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var themeDataStore: ThemeDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isDark by themeDataStore.isDarkTheme
                .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(5000), false)
                .collectAsState()

            FitnessAppTheme(darkTheme = isDark) {
                AppNavHost(authRepository)
            }
        }
    }
}

@Composable
private fun AppNavHost(authRepository: AuthRepository) {
    val navController = rememberNavController()

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val token = authRepository.getToken()
        startDestination = if (token != null) Routes.MAIN else Routes.LOGIN
    }

    if (startDestination == null) return

    NavHost(navController = navController, startDestination = startDestination!!) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.MAIN) {
            MainScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
                onBookingClick = { id ->
                    navController.navigate(Routes.bookingDetail(id))
                }
            )
        }
        composable(
            route = Routes.BOOKING_DETAIL,
            arguments = listOf(navArgument("bookingId") { type = NavType.LongType })
        ) {
            BookingDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
