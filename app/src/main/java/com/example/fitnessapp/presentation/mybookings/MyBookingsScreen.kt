package com.example.fitnessapp.presentation.mybookings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnessapp.domain.model.Booking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    onBookingClick: (Long) -> Unit,
    viewModel: MyBookingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pendingDeleteId by viewModel.pendingDeleteId.collectAsState()
    val deletingIds by viewModel.deletingIds.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.snackbarShown()
        }
    }

    // Диалог подтверждения удаления
    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Отменить запись?") },
            text = { Text("Вы уверены, что хотите отменить запись на это занятие?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete(pendingDeleteId!!) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Отменить запись") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDelete) { Text("Назад") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Мои записи") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
        when (val state = uiState) {
            is MyBookingsUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is MyBookingsUiState.Empty -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Вы ещё не записаны ни на одно занятие",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            is MyBookingsUiState.Success -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.bookings, key = { it.id }) { booking ->
                        MyBookingCard(
                            booking = booking,
                            isDeleting = booking.id in deletingIds,
                            onClick = { onBookingClick(booking.id) },
                            onLongClick = { viewModel.onLongPress(booking.id) }
                        )
                    }
                }
            }

            is MyBookingsUiState.Error -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
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
        } // PullToRefreshBox
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MyBookingCard(
    booking: Booking,
    isDeleting: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(booking.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Время: ${booking.time.replace("T", " ")}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Участников: ${booking.clientIds.size} / ${booking.slots}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isDeleting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
    }
}
