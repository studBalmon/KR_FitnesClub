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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Science
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
    onOpenCatalogs: (() -> Unit)? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val accent by viewModel.accentColor.collectAsState()
    val testDataPresent by viewModel.testDataPresent.collectAsState()
    val testDataBusy by viewModel.testDataBusy.collectAsState()
    val testDataMessage by viewModel.testDataMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val isAdmin = onOpenCatalogs != null

    LaunchedEffect(isAdmin) {
        if (isAdmin) viewModel.loadTestDataStatus()
    }

    LaunchedEffect(testDataMessage) {
        testDataMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.testDataMessageShown()
        }
    }

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
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                actions = {
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Выйти",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is ProfileUiState.Error -> {
                Box(Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center) {
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
                    onLogout = { viewModel.logout(onLogout) },
                    onOpenCatalogs = onOpenCatalogs,
                    testDataPresent = testDataPresent,
                    testDataBusy = testDataBusy,
                    onToggleTestData = viewModel::toggleTestData
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
    onLogout: () -> Unit,
    onOpenCatalogs: (() -> Unit)? = null,
    testDataPresent: Boolean? = null,
    testDataBusy: Boolean = false,
    onToggleTestData: () -> Unit = {}
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
        if (profile.cardEndDate != null) {
            SubscriptionCard(profile.cardEndDate)
        }

        Text(
            "Личные данные", style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Тёмная тема", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = isDark, onCheckedChange = { onToggleTheme() })
        }

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

        if (onOpenCatalogs != null) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                "Управление", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ManagementRow(
                icon = Icons.Default.FitnessCenter,
                title = "Справочники",
                subtitle = "Типы тренеров и занятий",
                onClick = onOpenCatalogs
            )

            // Тестовые данные
            val isPresent = testDataPresent == true
            Button(
                onClick = onToggleTestData,
                enabled = !testDataBusy && testDataPresent != null,
                modifier = Modifier.fillMaxWidth(),
                colors = if (isPresent)
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                else ButtonDefaults.buttonColors()
            ) {
                if (testDataBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        if (isPresent) Icons.Default.Delete else Icons.Default.Science,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isPresent) "Удалить тестовые данные"
                        else "Добавить тестовые данные"
                    )
                }
            }
            Text(
                "Демо-набор для проверки всех функций: тренеры, клиенты с разными абонементами, занятия и записи.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManagementRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                if (selected) Modifier.border(
                    3.dp,
                    MaterialTheme.colorScheme.onSurface,
                    CircleShape
                )
                else Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    CircleShape
                )
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
    } catch (_: Exception) {
        false
    }

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
