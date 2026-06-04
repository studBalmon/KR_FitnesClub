package com.example.fitnessapp.presentation.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnessapp.domain.model.Booking
import com.example.fitnessapp.presentation.common.CalendarSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingsScreen(
    onEditBooking: (Long) -> Unit,
    onViewParticipants: (Long) -> Unit,
    viewModel: AdminBookingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pendingDeleteId by viewModel.pendingDeleteId.collectAsState()
    val deletingIds by viewModel.deletingIds.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val datesWithBookings by viewModel.datesWithBookings.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val coaches by viewModel.coaches.collectAsState()
    val workoutTypes by viewModel.workoutTypes.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var filterSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.snackbarShown()
        }
    }

    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Удалить занятие?") },
            text = { Text("Занятие будет удалено для всех записавшихся участников.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete(pendingDeleteId!!) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDelete) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Все занятия") },
                actions = {
                    val isActive = filterState != AdminFilterState()
                    IconButton(onClick = { filterSheetVisible = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Фильтры",
                            tint = if (isActive) MaterialTheme.colorScheme.primary
                            else LocalContentColor.current
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Поиск по названию...") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Очистить")
                            }
                        }
                    }
                )

                CalendarSection(
                    selectedDate = selectedDate,
                    bookingCounts = datesWithBookings,
                    onDateSelected = viewModel::selectDate
                )

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (val state = uiState) {
                        is AdminBookingsUiState.Loading -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }

                        is AdminBookingsUiState.Empty -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Box(
                                        Modifier.fillParentMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            if (searchQuery.isEmpty()) "На этот день занятий нет"
                                            else "Ничего не найдено по запросу «$searchQuery»",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        is AdminBookingsUiState.Success -> {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.bookings, key = { it.id }) { booking ->
                                    AdminBookingCard(
                                        booking = booking,
                                        isDeleting = booking.id in deletingIds,
                                        onEdit = { onEditBooking(booking.id) },
                                        onViewParticipants = { onViewParticipants(booking.id) },
                                        onLongClick = { viewModel.onLongPress(booking.id) }
                                    )
                                }
                            }
                        }

                        is AdminBookingsUiState.Error -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(state.message, color = MaterialTheme.colorScheme.error)
                                    Button(onClick = viewModel::load) { Text("Повторить") }
                                }
                            }
                        }
                    }
                }
            }

            AdminFilterSheet(
                visible = filterSheetVisible,
                currentFilter = filterState,
                coaches = coaches,
                workoutTypes = workoutTypes,
                onApply = viewModel::applyFilterState,
                onDismiss = { filterSheetVisible = false }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AdminBookingCard(
    booking: Booking,
    isDeleting: Boolean,
    onEdit: () -> Unit,
    onViewParticipants: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Время: ${booking.time.replace("T", " ")}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Group, contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${booking.clientIds.size} / ${booking.slots}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (booking.isFull) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "Тренер #${booking.coachId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onViewParticipants, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Участники")
                }
                Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Изменить")
                }
            }
        }
    }
}
