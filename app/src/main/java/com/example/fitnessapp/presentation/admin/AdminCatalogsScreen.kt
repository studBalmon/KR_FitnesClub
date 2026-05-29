package com.example.fitnessapp.presentation.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnessapp.domain.model.CoachType
import com.example.fitnessapp.domain.model.WorkoutItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCatalogsScreen(
    viewModel: AdminCatalogsViewModel = hiltViewModel()
) {
    val coachTypes by viewModel.coachTypes.collectAsState()
    val coachTypesLoading by viewModel.coachTypesLoading.collectAsState()
    val workouts by viewModel.workouts.collectAsState()
    val workoutsLoading by viewModel.workoutsLoading.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Типы тренеров", "Типы занятий")

    // Диалоги
    var coachTypeDialog by remember { mutableStateOf<CoachType?>(null) }
    var showCoachTypeDialog by remember { mutableStateOf(false) }
    var workoutDialog by remember { mutableStateOf<WorkoutItem?>(null) }
    var showWorkoutDialog by remember { mutableStateOf(false) }

    // Подтверждение удаления
    var pendingDeleteCoachType by remember { mutableStateOf<CoachType?>(null) }
    var pendingDeleteWorkout by remember { mutableStateOf<WorkoutItem?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { snackbarHostState.showSnackbar(it); viewModel.snackbarShown() }
    }

    // Диалоги удаления
    pendingDeleteCoachType?.let { ct ->
        AlertDialog(
            onDismissRequest = { pendingDeleteCoachType = null },
            title = { Text("Удалить тип тренера?") },
            text = { Text("«${ct.name}» будет удалён.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteCoachType(ct.id); pendingDeleteCoachType = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { pendingDeleteCoachType = null }) { Text("Отмена") } }
        )
    }
    pendingDeleteWorkout?.let { w ->
        AlertDialog(
            onDismissRequest = { pendingDeleteWorkout = null },
            title = { Text("Удалить тип занятия?") },
            text = { Text("«${w.name}» будет удалён.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteWorkout(w.id); pendingDeleteWorkout = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { pendingDeleteWorkout = null }) { Text("Отмена") } }
        )
    }

    if (showCoachTypeDialog) {
        CoachTypeDialog(
            item = coachTypeDialog,
            onDismiss = { showCoachTypeDialog = false; coachTypeDialog = null },
            onSave = { name ->
                if (coachTypeDialog == null) viewModel.createCoachType(name)
                else viewModel.updateCoachType(coachTypeDialog!!.id, name)
                showCoachTypeDialog = false; coachTypeDialog = null
            }
        )
    }
    if (showWorkoutDialog) {
        WorkoutDialog(
            item = workoutDialog,
            coachTypes = coachTypes,
            onDismiss = { showWorkoutDialog = false; workoutDialog = null },
            onSave = { name, desc, dur, ctId ->
                if (workoutDialog == null) viewModel.createWorkout(name, desc, dur, ctId)
                else viewModel.updateWorkout(workoutDialog!!.id, name, desc, dur, ctId)
                showWorkoutDialog = false; workoutDialog = null
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Справочники") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (selectedTab == 0) { coachTypeDialog = null; showCoachTypeDialog = true }
                else { workoutDialog = null; showWorkoutDialog = true }
            }) { Icon(Icons.Default.Add, contentDescription = "Добавить") }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                if (index == 0) Icons.Default.Person else Icons.Default.FitnessCenter,
                                contentDescription = null
                            )
                        },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> CoachTypesTab(
                    items = coachTypes,
                    loading = coachTypesLoading,
                    onEdit = { coachTypeDialog = it; showCoachTypeDialog = true },
                    onDelete = { pendingDeleteCoachType = it }
                )
                1 -> WorkoutsTab(
                    items = workouts,
                    coachTypes = coachTypes,
                    loading = workoutsLoading,
                    onEdit = { workoutDialog = it; showWorkoutDialog = true },
                    onDelete = { pendingDeleteWorkout = it }
                )
            }
        }
    }
}

// ── Вкладка типов тренеров ────────────────────────────────────────────────────

@Composable
private fun CoachTypesTab(
    items: List<CoachType>,
    loading: Boolean,
    onEdit: (CoachType) -> Unit,
    onDelete: (CoachType) -> Unit
) {
    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Нет типов тренеров. Нажмите + чтобы добавить.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items, key = { it.id }) { ct ->
            SimpleItemCard(
                title = ct.name,
                subtitle = "ID: ${ct.id}",
                onEdit = { onEdit(ct) },
                onDelete = { onDelete(ct) }
            )
        }
    }
}

// ── Вкладка типов занятий ─────────────────────────────────────────────────────

@Composable
private fun WorkoutsTab(
    items: List<WorkoutItem>,
    coachTypes: List<CoachType>,
    loading: Boolean,
    onEdit: (WorkoutItem) -> Unit,
    onDelete: (WorkoutItem) -> Unit
) {
    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Нет типов занятий. Нажмите + чтобы добавить.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items, key = { it.id }) { w ->
            val ctName = coachTypes.find { it.id == w.coachTypeId }?.name ?: "ID ${w.coachTypeId}"
            SimpleItemCard(
                title = w.name,
                subtitle = buildString {
                    append("Тип тренера: $ctName • ${w.duration} мин")
                    if (!w.description.isNullOrBlank()) append("\n${w.description}")
                },
                onEdit = { onEdit(w) },
                onDelete = { onDelete(w) }
            )
        }
    }
}

// ── Общая карточка элемента ───────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SimpleItemCard(
    title: String,
    subtitle: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onEdit, onLongClick = onDelete),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (subtitle.isNotBlank())
                    Text(subtitle, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onEdit) { Text("Изменить") }
        }
    }
}

// ── Диалог типа тренера ───────────────────────────────────────────────────────

@Composable
private fun CoachTypeDialog(
    item: CoachType?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember(item) { mutableStateOf(item?.name ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Новый тип тренера" else "Редактировать") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onSave(name.trim()) }, enabled = name.isNotBlank()) {
                Text(if (item == null) "Добавить" else "Сохранить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

// ── Диалог типа занятия ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDialog(
    item: WorkoutItem?,
    coachTypes: List<CoachType>,
    onDismiss: () -> Unit,
    onSave: (String, String?, Int, Int) -> Unit
) {
    var name        by remember(item) { mutableStateOf(item?.name ?: "") }
    var description by remember(item) { mutableStateOf(item?.description ?: "") }
    var durationStr by remember(item) { mutableStateOf(item?.duration?.toString() ?: "") }
    var selectedCt  by remember(item, coachTypes) {
        mutableStateOf(
            if (item != null) coachTypes.find { it.id == item.coachTypeId } ?: coachTypes.firstOrNull()
            else coachTypes.firstOrNull()
        )
    }
    var ctMenuOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Новый тип занятия" else "Редактировать") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2, maxLines = 4
                )
                OutlinedTextField(
                    value = durationStr,
                    onValueChange = { durationStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Длительность (мин) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (coachTypes.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = ctMenuOpen, onExpandedChange = { ctMenuOpen = it }) {
                        OutlinedTextField(
                            value = selectedCt?.name ?: "—",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Тип тренера *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(ctMenuOpen) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = ctMenuOpen, onDismissRequest = { ctMenuOpen = false }) {
                            coachTypes.forEach { ct ->
                                DropdownMenuItem(
                                    text = { Text(ct.name) },
                                    onClick = { selectedCt = ct; ctMenuOpen = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val dur = durationStr.toIntOrNull() ?: 0
            val valid = name.isNotBlank() && dur > 0 && selectedCt != null
            Button(onClick = {
                onSave(name.trim(), description.trim().ifBlank { null }, dur, selectedCt!!.id)
            }, enabled = valid) {
                Text(if (item == null) "Добавить" else "Сохранить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
