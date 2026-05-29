package com.example.fitnessapp.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnessapp.presentation.theme.AccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val accent by viewModel.accentColor.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is SaveState.Success -> {
                snackbarHostState.showSnackbar("Профиль сохранён")
                viewModel.saveStateSeen()
            }
            is SaveState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.saveStateSeen()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Профиль") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is ProfileUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = viewModel::loadProfile) { Text("Повторить") }
                    }
                }
            }

            is ProfileUiState.Success -> {
                ProfileContent(
                    profile = state.profile,
                    isDark = isDark,
                    accent = accent,
                    isSaving = saveState is SaveState.Loading,
                    padding = padding,
                    onSave = viewModel::saveProfile,
                    onToggleTheme = viewModel::toggleTheme,
                    onAccentSelected = viewModel::setAccentColor,
                    onLogout = { viewModel.logout(onLogout) }
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: com.example.fitnessapp.domain.model.UserProfile,
    isDark: Boolean,
    accent: AccentColor,
    isSaving: Boolean,
    padding: PaddingValues,
    onSave: (String, String, String) -> Unit,
    onToggleTheme: () -> Unit,
    onAccentSelected: (AccentColor) -> Unit,
    onLogout: () -> Unit
) {
    var fio by remember(profile.fio) { mutableStateOf(profile.fio) }
    var phone by remember(profile.phone) { mutableStateOf(profile.phone) }
    var email by remember(profile.email) { mutableStateOf(profile.email) }

    val hasChanges = fio != profile.fio || phone != profile.phone || email != profile.email

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Абонемент ─────────────────────────────────────────────────────────
        if (profile.cardEndDate != null) {
            SubscriptionCard(profile.cardEndDate)
        }

        // ── Данные профиля ────────────────────────────────────────────────────
        Text("Личные данные", style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = fio,
            onValueChange = { fio = it },
            label = { Text("ФИО") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Телефон") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Электронная почта") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onSave(fio.trim(), phone.trim(), email.trim()) },
            enabled = hasChanges && !isSaving &&
                    fio.isNotBlank() && phone.isNotBlank() && email.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Сохранить")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── Тёмная тема ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Тёмная тема", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = isDark, onCheckedChange = { onToggleTheme() })
        }

        // ── Цветовой акцент ───────────────────────────────────────────────────
        Text(
            "Цветовой акцент",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AccentColor.entries.forEach { color ->
                AccentSwatch(
                    color = color,
                    selected = color == accent,
                    onClick = { onAccentSelected(color) }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── Выход ─────────────────────────────────────────────────────────────
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Выйти из аккаунта")
        }
    }
}

@Composable
private fun AccentSwatch(
    color: AccentColor,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color.previewColor)
            .then(
                if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SubscriptionCard(cardEndDate: String) {
    // Проверяем истёк ли абонемент
    val isExpired = try {
        java.time.LocalDate.parse(cardEndDate).isBefore(java.time.LocalDate.now())
    } catch (_: Exception) { false }

    val containerColor = if (isExpired) MaterialTheme.colorScheme.errorContainer
                         else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isExpired) MaterialTheme.colorScheme.onErrorContainer
                       else MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isExpired) "Абонемент истёк" else "Абонемент активен",
                style = MaterialTheme.typography.titleSmall,
                color = contentColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Действует до: $cardEndDate",
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}
