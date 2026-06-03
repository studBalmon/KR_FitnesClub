package com.example.fitnessapp.presentation.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnessapp.domain.model.AdminUser
import com.example.fitnessapp.domain.model.CoachType
import java.time.format.DateTimeFormatter

// Названия ролей для UI и их userTypeId
private val ROLES = listOf(
    Triple(3, "CLIENT", "Клиент"),
    Triple(2, "COACH", "Тренер"),
    Triple(1, "ADMIN", "Администратор")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    viewModel: AdminUsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val coachTypes by viewModel.coachTypes.collectAsState()
    val pendingDeleteId by viewModel.pendingDeleteId.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Открытый диалог: null = закрыт, null user = создание, non-null = редактирование
    var dialogUser by remember { mutableStateOf<AdminUser?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // Клиент, которому продлеваем абонемент (null = диалог закрыт)
    var extendUser by remember { mutableStateOf<AdminUser?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.snackbarShown()
        }
    }

    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Удалить пользователя?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete(pendingDeleteId!!) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissDelete) { Text("Отмена") } }
        )
    }

    // Диалог продления абонемента
    extendUser?.let { user ->
        ExtendSubscriptionDialog(
            user      = user,
            onDismiss = { extendUser = null },
            onConfirm = { months ->
                viewModel.extendSubscription(user.id, months)
                extendUser = null
            }
        )
    }

    // Диалог создания / редактирования
    if (showDialog) {
        UserDialog(
            user = dialogUser,
            coachTypes = coachTypes,
            onDismiss = { showDialog = false; dialogUser = null },
            onCreate = { fio, phone, email, password, userTypeId, coachTypeId ->
                viewModel.createUser(fio, phone, email, password, userTypeId, coachTypeId)
                showDialog = false
            },
            onUpdate = { fio, phone, email, newPassword, coachTypeId ->
                viewModel.updateUser(dialogUser!!.id, fio, phone, email, newPassword, coachTypeId)
                showDialog = false; dialogUser = null
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Пользователи") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { dialogUser = null; showDialog = true }) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Добавить")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Поиск по ФИО, email, телефону...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty())
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, null)
                        }
                }
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is AdminUsersUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is AdminUsersUiState.Empty -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                if (searchQuery.isEmpty()) "Нет пользователей"
                                else "Ничего не найдено",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is AdminUsersUiState.Success -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.users, key = { it.id }) { user ->
                                UserCard(
                                    user = user,
                                    coachTypes = coachTypes,
                                    onEdit = { dialogUser = user; showDialog = true },
                                    onLongClick = { viewModel.onLongPress(user.id) },
                                    onExtend = { extendUser = user }
                                )
                            }
                        }
                    }

                    is AdminUsersUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message, color = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.height(8.dp))
                                Button(onClick = viewModel::load) { Text("Повторить") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserCard(
    user: AdminUser,
    coachTypes: List<CoachType>,
    onEdit: () -> Unit,
    onLongClick: () -> Unit,
    onExtend: () -> Unit
) {
    val coachTypeName = if (user.roleName == "COACH") {
        coachTypes.find { it.id == user.coachTypeId }?.name ?: "—"
    } else null

    val isClient = user.roleName == "CLIENT"

    // Подсветка карточки в зависимости от состояния абонемента
    val cardColor = when {
        user.isExpired      -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        user.isExpiringSoon -> ExpiringContainer
        else                -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onEdit, onLongClick = onLongClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = roleColor(user.roleName),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        user.fio.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.fio, style = MaterialTheme.typography.titleSmall)
                Text(
                    user.email, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    user.phone, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (coachTypeName != null) {
                    Text(
                        "Тип: $coachTypeName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (isClient && user.cardEndDate != null) {
                    SubscriptionLine(user)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(user.roleLabel, style = MaterialTheme.typography.labelSmall) }
                )
                if (isClient) {
                    Spacer(Modifier.height(4.dp))
                    FilledTonalIconButton(
                        onClick = onExtend,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreTime,
                            contentDescription = "Продлить абонемент",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionLine(user: AdminUser) {
    val days = user.daysLeft ?: return
    val dateStr = user.cardEndDate?.format(DATE_FORMAT) ?: ""
    val (text, color) = when {
        days < 0  -> "Абонемент истёк ($dateStr)" to MaterialTheme.colorScheme.error
        days == 0L -> "Истекает сегодня" to ExpiringText
        days == 1L -> "Истекает завтра ($dateStr)" to ExpiringText
        days <= 6  -> "Истекает через $days дн. ($dateStr)" to ExpiringText
        else      -> "Абонемент до $dateStr" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
        Icon(
            Icons.Default.CardMembership,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ExtendSubscriptionDialog(
    user: AdminUser,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val options = listOf(1, 3, 6, 12)
    var selected by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Продлить абонемент") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(user.fio, style = MaterialTheme.typography.titleSmall)
                user.cardEndDate?.let {
                    Text(
                        if (user.isExpired) "Истёк: ${it.format(DATE_FORMAT)}"
                        else "Действует до: ${it.format(DATE_FORMAT)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("На сколько продлить?", style = MaterialTheme.typography.bodyMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { m ->
                        FilterChip(
                            selected = selected == m,
                            onClick = { selected = m },
                            label = { Text(monthsLabel(m)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selected) }) { Text("Продлить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

private fun monthsLabel(m: Int): String = when (m) {
    1 -> "1 месяц"
    in 2..4 -> "$m месяца"
    else -> "$m месяцев"
}

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val ExpiringContainer = Color(0xFFFFF3E0)  // мягкий янтарный фон
private val ExpiringText = Color(0xFFE65100)        // насыщенный оранжевый текст

@Composable
private fun roleColor(roleName: String) = when (roleName) {
    "ADMIN" -> MaterialTheme.colorScheme.error
    "COACH" -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.secondary
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDialog(
    user: AdminUser?,          // null = создание, non-null = редактирование
    coachTypes: List<CoachType>,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, Int, Int?) -> Unit,
    onUpdate: (String, String, String, String?, Int?) -> Unit
) {
    val isEdit = user != null

    var fio by remember(user) { mutableStateOf(user?.fio ?: "") }
    var phone by remember(user) { mutableStateOf(user?.phone ?: "") }
    var email by remember(user) { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // только для создания
    var selectedRole by remember { mutableStateOf(ROLES.first()) } // CLIENT по умолчанию
    var roleMenuOpen by remember { mutableStateOf(false) }
    val currentRoleName = user?.roleName ?: selectedRole.second

    var selectedCoachType by remember(user, coachTypes) {
        mutableStateOf(
            if (user != null && coachTypes.isNotEmpty())
                coachTypes.find { it.id == user.coachTypeId } ?: coachTypes.firstOrNull()
            else coachTypes.firstOrNull()
        )
    }
    var coachTypeMenuOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Редактировать пользователя" else "Новый пользователь") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = fio,
                    onValueChange = { fio = it },
                    label = { Text("ФИО *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Телефон *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isEdit) "Новый пароль (пусто = не менять)" else "Пароль *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword)
                                    Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )

                // Роль — только при создании
                if (!isEdit) {
                    ExposedDropdownMenuBox(
                        expanded = roleMenuOpen,
                        onExpandedChange = { roleMenuOpen = it }
                    ) {
                        OutlinedTextField(
                            value = selectedRole.third,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Роль *") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(roleMenuOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = roleMenuOpen,
                            onDismissRequest = { roleMenuOpen = false }
                        ) {
                            ROLES.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role.third) },
                                    onClick = { selectedRole = role; roleMenuOpen = false }
                                )
                            }
                        }
                    }
                }

                // только если роль COACH
                val showCoachType = currentRoleName == "COACH"
                if (showCoachType && coachTypes.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = coachTypeMenuOpen,
                        onExpandedChange = { coachTypeMenuOpen = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCoachType?.name ?: "—",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Тип тренера *") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    coachTypeMenuOpen
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = coachTypeMenuOpen,
                            onDismissRequest = { coachTypeMenuOpen = false }
                        ) {
                            coachTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        selectedCoachType = type; coachTypeMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val isCoach = currentRoleName == "COACH"
            val isValid = fio.isNotBlank() && phone.isNotBlank() && email.isNotBlank() &&
                    (isEdit || password.isNotBlank()) &&
                    (!isCoach || selectedCoachType != null)

            Button(
                onClick = {
                    if (isEdit) {
                        onUpdate(
                            fio.trim(), phone.trim(), email.trim(),
                            password.trim().ifBlank { null },
                            if (isCoach) selectedCoachType?.id else null
                        )
                    } else {
                        onCreate(
                            fio.trim(), phone.trim(), email.trim(),
                            password.trim(),
                            selectedRole.first,
                            if (isCoach) selectedCoachType?.id else null
                        )
                    }
                },
                enabled = isValid
            ) { Text(if (isEdit) "Сохранить" else "Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
