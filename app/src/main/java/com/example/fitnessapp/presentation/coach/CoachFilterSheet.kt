package com.example.fitnessapp.presentation.coach

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun CoachFilterSheet(
    visible: Boolean,
    currentFilter: CoachFilterState,
    workoutTypes: List<CoachWorkoutTypeItem>,
    onApply: (CoachFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var pendingWorkoutIds by remember(
        currentFilter
    ) {
        mutableStateOf(currentFilter.workoutIds)
    }
    var showWorkoutPicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable {
                        when {
                            showWorkoutPicker -> showWorkoutPicker = false
                            else -> onDismiss()
                        }
                    }
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            CoachFilterPanelContent(
                currentFilter = currentFilter,
                workoutTypes = workoutTypes,
                pendingWorkoutIds = pendingWorkoutIds,
                onWorkoutIdsChange = { pendingWorkoutIds = it },
                onOpenWorkoutPicker = { showWorkoutPicker = true },
                onApply = onApply,
                onDismiss = onDismiss
            )
        }

        AnimatedVisibility(
            visible = visible && showWorkoutPicker,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            CoachWorkoutPickerPanel(
                workoutTypes = workoutTypes,
                selectedWorkoutIds = pendingWorkoutIds,
                onConfirm = { pendingWorkoutIds = it; showWorkoutPicker = false },
                onDismiss = { showWorkoutPicker = false }
            )
        }
    }
}

@Composable
private fun CoachFilterPanelContent(
    currentFilter: CoachFilterState,
    workoutTypes: List<CoachWorkoutTypeItem>,
    pendingWorkoutIds: Set<Int>,
    onWorkoutIdsChange: (Set<Int>) -> Unit,
    onOpenWorkoutPicker: () -> Unit,
    onApply: (CoachFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSort by remember(
        currentFilter
    ) {
        mutableStateOf(currentFilter.sort)
    }
    var sortExpanded by remember { mutableStateOf(false) }
    var slotsFromText by remember(currentFilter) {
        mutableStateOf(
            currentFilter.slotsFrom?.toString() ?: ""
        )
    }
    var slotsToText by remember(currentFilter) {
        mutableStateOf(
            currentFilter.slotsTo?.toString() ?: ""
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Text("Сортировка и фильтры", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть")
                }
            }

            HorizontalDivider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Сортировка
                Column(modifier = Modifier.animateContentSize()) {
                    CoachFilterRow(
                        label = "Сортировка",
                        value = if (
                            selectedSort != CoachBookingSort.DEFAULT)
                            selectedSort.labelRu
                        else "По умолчанию",
                        valueActive = selectedSort != CoachBookingSort.DEFAULT,
                        trailingIcon = {
                            Icon(
                                if (sortExpanded)
                                    Icons.Default.ArrowDropUp
                                else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = { sortExpanded = !sortExpanded }
                    )
                    if (sortExpanded) {
                        Column(
                            modifier = Modifier.padding(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 4.dp
                            )
                        ) {
                            CoachBookingSort.entries.forEach { sort ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedSort = sort }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedSort == sort,
                                        onClick = { selectedSort = sort }
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(sort.labelRu, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                CoachFilterRow(
                    label = "Тип занятия",
                    value = if (pendingWorkoutIds.isEmpty()) "Любые"
                    else "Выбрано ${pendingWorkoutIds.size}",
                    valueActive = pendingWorkoutIds.isNotEmpty(),
                    onClick = onOpenWorkoutPicker
                )

                HorizontalDivider()

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Свободных мест",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CoachCompactField(
                            value = slotsFromText,
                            onValueChange = { v -> slotsFromText = v.filter { it.isDigit() } },
                            placeholder = "От",
                            modifier = Modifier.weight(1f)
                        )
                        Text("—", style = MaterialTheme.typography.bodyLarge)
                        CoachCompactField(
                            value = slotsToText,
                            onValueChange = { v -> slotsToText = v.filter { it.isDigit() } },
                            placeholder = "До",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            HorizontalDivider()
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedSort = CoachBookingSort.DEFAULT
                        sortExpanded = false
                        onWorkoutIdsChange(emptySet())
                        slotsFromText = ""
                        slotsToText = ""
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Сбросить") }

                Button(
                    onClick = {
                        onApply(
                            CoachFilterState(
                                sort = selectedSort,
                                workoutIds = pendingWorkoutIds,
                                slotsFrom = slotsFromText.toIntOrNull(),
                                slotsTo = slotsToText.toIntOrNull()
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Применить") }
            }
        }
    }
}

@Composable
private fun CoachFilterRow(
    label: String,
    value: String,
    valueActive: Boolean,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = if (valueActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        if (trailingIcon != null) trailingIcon()
        else Icon(
            Icons.Default.ArrowDropDown, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CoachWorkoutPickerPanel(
    workoutTypes: List<CoachWorkoutTypeItem>,
    selectedWorkoutIds: Set<Int>,
    onConfirm: (Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    var pending by remember(
        selectedWorkoutIds
    ) {
        mutableStateOf(selectedWorkoutIds)
    }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Тип занятия", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Назад")
                }
            }

            HorizontalDivider()

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(workoutTypes, key = { it.id }) { workout ->
                    val checked = workout.id in pending
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                pending =
                                    if (checked) pending - workout.id else pending + workout.id
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                pending = if (it) pending + workout.id else pending - workout.id
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            workout.name,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        pending = emptySet()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сбросить")
                }
                Button(onClick = { onConfirm(pending) }, modifier = Modifier.weight(1f)) {
                    Text("Выбрать")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoachCompactField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = OutlinedTextFieldDefaults.colors()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        interactionSource = interactionSource,
        modifier = modifier.height(42.dp)
    ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            value = value,
            innerTextField = innerTextField,
            enabled = true,
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            colors = colors,
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = colors
                )
            }
        )
    }
}
