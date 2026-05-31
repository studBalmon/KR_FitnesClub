package com.example.fitnessapp.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnessapp.domain.model.Booking
import com.example.fitnessapp.presentation.common.CalendarSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onBookingClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState           by viewModel.uiState.collectAsState()
    val query             by viewModel.query.collectAsState()
    val history           by viewModel.history.collectAsState()
    val joiningIds        by viewModel.joiningIds.collectAsState()
    val myBookingIds      by viewModel.myBookingIds.collectAsState()
    val isRefreshing      by viewModel.isRefreshing.collectAsState()
    val snackbarMessage   by viewModel.snackbarMessage.collectAsState()
    val selectedDate      by viewModel.selectedDate.collectAsState()
    val datesWithBookings by viewModel.datesWithBookings.collectAsState()
    val filterState       by viewModel.filterState.collectAsState()
    val workoutTypes      by viewModel.workoutTypes.collectAsState()
    val coaches           by viewModel.coaches.collectAsState()

    val snackbarHostState  = remember { SnackbarHostState() }
    var isSearchFocused    by remember { mutableStateOf(false) }
    var filterSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { snackbarHostState.showSnackbar(it); viewModel.snackbarShown() }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh    = viewModel::refresh,
            modifier     = Modifier.padding(padding).fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Поисковая строка с выпадающей историей ───────────────────
                val showHistory = isSearchFocused && query.isEmpty() && history.isNotEmpty()
                val isFilterActive = filterState != ClientFilterState()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
                ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value         = query,
                        onValueChange = viewModel::onQueryChange,
                        modifier      = Modifier.fillMaxWidth()
                            .onFocusChanged { isSearchFocused = it.isFocused },
                        placeholder   = { Text("Поиск занятий...") },
                        singleLine    = true,
                        leadingIcon   = {
                            IconButton(onClick = { viewModel.search() }) {
                                Icon(Icons.Default.Search, contentDescription = "Поиск")
                            }
                        },
                        trailingIcon  = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = viewModel::clearQuery) {
                                    Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                }
                            }
                        }
                    )
                    DropdownMenu(
                        expanded         = showHistory,
                        onDismissRequest = { isSearchFocused = false },
                        modifier         = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text("Недавние запросы", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = viewModel::clearHistory) { Text("Очистить") }
                        }
                        HorizontalDivider()
                        history.forEach { item ->
                            DropdownMenuItem(
                                text         = { Text(item) },
                                leadingIcon  = {
                                    Icon(Icons.Default.History, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { viewModel.selectHistoryItem(item) }) {
                                        Icon(Icons.Default.Search, contentDescription = "Искать",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = { viewModel.selectHistoryItem(item) }
                            )
                        }
                    }
                } // Box (поисковая строка)

                IconButton(onClick = { filterSheetVisible = true }) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Фильтры",
                        tint = if (isFilterActive) MaterialTheme.colorScheme.primary
                               else LocalContentColor.current
                    )
                }
                } // Row

                // ── Календарь (скрывается при активном поиске) ───────────────
                val isSearching = uiState is HomeUiState.SearchResults ||
                        uiState is HomeUiState.SearchLoading ||
                        uiState is HomeUiState.SearchEmpty
                if (!isSearching) {
                    CalendarSection(
                        selectedDate   = selectedDate,
                        bookingCounts  = datesWithBookings,
                        onDateSelected = viewModel::selectDate
                    )
                }

                // ── Контент ───────────────────────────────────────────────────
                when (val state = uiState) {
                    is HomeUiState.Loading, HomeUiState.SearchLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is HomeUiState.AllBookings -> {
                        BookingList(
                            bookings     = state.bookings,
                            joiningIds   = joiningIds,
                            myBookingIds = myBookingIds,
                            onJoin       = viewModel::joinBooking,
                            onCardClick  = onBookingClick,
                            emptyText    = "На этот день занятий нет"
                        )
                    }
                    is HomeUiState.SearchResults -> {
                        BookingList(
                            bookings     = state.bookings,
                            joiningIds   = joiningIds,
                            myBookingIds = myBookingIds,
                            onJoin       = viewModel::joinBooking,
                            onCardClick  = onBookingClick,
                            emptyText    = ""
                        )
                    }
                    HomeUiState.SearchEmpty -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Ничего не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    is HomeUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(state.message, color = MaterialTheme.colorScheme.error)
                                Button(onClick = {
                                    if (query.isBlank()) viewModel.loadAllBookings()
                                    else viewModel.search()
                                }) { Text("Повторить") }
                            }
                        }
                    }
                }
            }
        }

        ClientFilterSheet(
            visible       = filterSheetVisible,
            currentFilter = filterState,
            coaches       = coaches,
            workoutTypes  = workoutTypes,
            onApply       = viewModel::applyFilterState,
            onDismiss     = { filterSheetVisible = false }
        )
    }
}

@Composable
private fun BookingList(
    bookings: List<Booking>,
    joiningIds: Set<Long>,
    myBookingIds: Set<Long>,
    onJoin: (Long) -> Unit,
    onCardClick: (Long) -> Unit,
    emptyText: String
) {
    if (bookings.isEmpty()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        return
    }
    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(bookings, key = { it.id }) { booking ->
            BookingCard(
                booking         = booking,
                isJoining       = booking.id in joiningIds,
                isAlreadyJoined = booking.id in myBookingIds,
                onJoin          = { onJoin(booking.id) },
                onClick         = { onCardClick(booking.id) }
            )
        }
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    isJoining: Boolean,
    isAlreadyJoined: Boolean,
    onJoin: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(booking.name, style = MaterialTheme.typography.titleMedium)
            Text("Время: ${booking.time.substringAfter("T").take(5)}",
                style = MaterialTheme.typography.bodySmall)
            Text(
                "Свободных мест: ${booking.availableSlots} / ${booking.slots}",
                style = MaterialTheme.typography.bodySmall,
                color = if (booking.isFull) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            when {
                isAlreadyJoined -> OutlinedButton(
                    onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()
                ) { Text("✓ Вы уже записаны") }
                isJoining -> Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                }
                booking.isFull -> Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                    Text("Мест нет")
                }
                else -> Button(onClick = onJoin, modifier = Modifier.fillMaxWidth()) {
                    Text("Записаться")
                }
            }
        }
    }
}
