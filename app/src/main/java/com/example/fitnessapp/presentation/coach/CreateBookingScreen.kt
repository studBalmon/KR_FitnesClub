package com.example.fitnessapp.presentation.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBookingScreen(
    onBack: () -> Unit,
    viewModel: CreateBookingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var name by remember { mutableStateOf("") }
    var slotsText by remember { mutableStateOf("") }
    var extra by remember { mutableStateOf("") }

    // Дата и время
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(10, 0)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is CreateBookingState.Success) onBack()
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Выберите время") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создать занятие") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название занятия *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = slotsText,
                onValueChange = { slotsText = it.filter { c -> c.isDigit() } },
                label = { Text("Количество мест *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Дата
            OutlinedTextField(
                value = selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                onValueChange = {},
                label = { Text("Дата *") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) { Text("Изменить") }
                }
            )

            // Время
            OutlinedTextField(
                value = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                onValueChange = {},
                label = { Text("Время *") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showTimePicker = true }) { Text("Изменить") }
                }
            )

            OutlinedTextField(
                value = extra,
                onValueChange = { extra = it },
                label = { Text("Описание / ссылка на видео (необязательно)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )

            if (state is CreateBookingState.Error) {
                Text(
                    (state as CreateBookingState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            val slots = slotsText.toIntOrNull() ?: 0
            val isValid = name.isNotBlank() && slots > 0
            val isLoading = state is CreateBookingState.Loading

            Button(
                onClick = {
                    val isoTime =
                        "${selectedDate}T${
                            selectedTime.format(
                                DateTimeFormatter.ofPattern("HH:mm:ss")
                            )
                        }"
                    viewModel.create(name.trim(), slots, extra.trim(), isoTime)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Создать занятие")
                }
            }
        }
    }
}
