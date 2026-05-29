package com.example.fitnessapp.presentation.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnessapp.domain.model.Booking
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookingScreen(
    onBack: () -> Unit,
    viewModel: EditBookingViewModel = hiltViewModel()
) {
    val loadState by viewModel.loadState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState is EditBookingSaveState.Success) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать занятие") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        when (val ls = loadState) {
            is EditBookingLoadState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is EditBookingLoadState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(ls.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = viewModel::load) { Text("Повторить") }
                    }
                }
            }
            is EditBookingLoadState.Loaded -> {
                EditBookingForm(
                    booking = ls.booking,
                    saveState = saveState,
                    modifier = Modifier.padding(padding),
                    onSave = { name, slots, extra, time -> viewModel.save(name, slots, extra, time) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBookingForm(
    booking: Booking,
    saveState: EditBookingSaveState,
    modifier: Modifier = Modifier,
    onSave: (String, Int, String?, String) -> Unit
) {
    val existingDateTime = remember(booking.time) {
        runCatching { java.time.LocalDateTime.parse(booking.time) }.getOrNull()
    }

    var name by remember(booking.name) { mutableStateOf(booking.name) }
    var slotsText by remember(booking.slots) { mutableStateOf(booking.slots.toString()) }
    var extra by remember(booking.extra) { mutableStateOf(booking.extra ?: "") }
    var selectedDate by remember { mutableStateOf(existingDateTime?.toLocalDate() ?: LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(existingDateTime?.toLocalTime() ?: LocalTime.of(10, 0)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay()
                .toInstant(ZoneOffset.UTC).toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }
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
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Отмена") } }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
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

        if (saveState is EditBookingSaveState.Error) {
            Text(
                saveState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        val slots = slotsText.toIntOrNull() ?: 0
        val isSaving = saveState is EditBookingSaveState.Saving

        Button(
            onClick = {
                val isoTime = "${selectedDate}T${selectedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"
                onSave(name.trim(), slots, extra.trim(), isoTime)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && slots > 0 && !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Сохранить изменения")
            }
        }
    }
}
