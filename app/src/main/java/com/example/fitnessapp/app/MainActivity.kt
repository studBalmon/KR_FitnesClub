package com.example.fitnessapp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.fitnessapp.data.local.ThemeDataStore
import com.example.fitnessapp.data.local.UserDataStore
import com.example.fitnessapp.domain.repository.AuthRepository
import com.example.fitnessapp.presentation.admin.AdminMainScreen
import com.example.fitnessapp.presentation.auth.LoginScreen
import com.example.fitnessapp.presentation.auth.RegisterScreen
import com.example.fitnessapp.presentation.booking.BookingDetailScreen
import com.example.fitnessapp.presentation.coach.CoachMainScreen
import com.example.fitnessapp.presentation.coach.CreateBookingScreen
import com.example.fitnessapp.presentation.coach.EditBookingScreen
import com.example.fitnessapp.presentation.coach.ParticipantsScreen
import com.example.fitnessapp.presentation.main.MainScreen
import com.example.fitnessapp.presentation.navigation.Routes
import com.example.fitnessapp.presentation.theme.FitnessAppTheme
import dagger.hilt.android.AndroidEntryPoint
import com.example.fitnessapp.presentation.theme.AccentColor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository
    @Inject
    lateinit var themeDataStore: ThemeDataStore
    @Inject
    lateinit var userDataStore: UserDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialDark = runBlocking { themeDataStore.isDarkTheme.first() }
        val initialAccent = runBlocking { themeDataStore.accentColor.first() }
        setContent {
            val isDark by themeDataStore.isDarkTheme
                .stateIn(lifecycleScope, SharingStarted.Eagerly, initialDark)
                .collectAsState()
            val accent by themeDataStore.accentColor
                .map { AccentColor.fromKey(it) }
                .stateIn(lifecycleScope, SharingStarted.Eagerly, AccentColor.fromKey(initialAccent))
                .collectAsState()
            FitnessAppTheme(darkTheme = isDark, accent = accent) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavHost(authRepository, userDataStore)
                }
            }
        }
    }
}

/** Возвращает начальный маршрут по userTypeId: 1=admin, 2=coach, иначе=client */
private fun homeRouteFor(userTypeId: Int) = when (userTypeId) {
    1 -> Routes.ADMIN_MAIN
    2 -> Routes.COACH_MAIN
    else -> Routes.MAIN
}

@Composable
private fun AppNavHost(authRepository: AuthRepository, userDataStore: UserDataStore) {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val token = authRepository.getToken()
        startDestination = if (token != null) {
            homeRouteFor(userDataStore.getUserTypeId())
        } else {
            Routes.LOGIN
        }
    }

    if (startDestination == null) return

    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = startDestination!!) {

        // ── Auth ──────────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    scope.launch {
                        val dest = homeRouteFor(userDataStore.getUserTypeId())
                        navController.navigate(dest) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
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

        // ── Клиент ────────────────────────────────────────────────────────────
        composable(Routes.MAIN) {
            MainScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
                onBookingClick = { id -> navController.navigate(Routes.bookingDetail(id)) }
            )
        }

        // ── Тренер ────────────────────────────────────────────────────────────
        composable(Routes.COACH_MAIN) {
            CoachMainScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.COACH_MAIN) { inclusive = true }
                    }
                },
                onCreateBooking = { navController.navigate(Routes.CREATE_BOOKING) },
                onEditBooking = { id -> navController.navigate(Routes.editBooking(id)) },
                onViewParticipants = { id -> navController.navigate(Routes.participants(id)) }
            )
        }

        // ── Администратор ─────────────────────────────────────────────────────
        composable(Routes.ADMIN_MAIN) {
            AdminMainScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ADMIN_MAIN) { inclusive = true }
                    }
                },
                onEditBooking = { id -> navController.navigate(Routes.editBooking(id)) },
                onViewParticipants = { id -> navController.navigate(Routes.participants(id)) }
            )
        }

        // ── Общие экраны (тренер + admin) ─────────────────────────────────────
        composable(Routes.CREATE_BOOKING) {
            CreateBookingScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.EDIT_BOOKING,
            arguments = listOf(navArgument("bookingId") { type = NavType.LongType })
        ) {
            EditBookingScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.PARTICIPANTS,
            arguments = listOf(navArgument("bookingId") { type = NavType.LongType })
        ) {
            ParticipantsScreen(onBack = { navController.popBackStack() })
        }

        // ── Детали занятия (клиент) ───────────────────────────────────────────
        composable(
            route = Routes.BOOKING_DETAIL,
            arguments = listOf(navArgument("bookingId") { type = NavType.LongType })
        ) {
            BookingDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
