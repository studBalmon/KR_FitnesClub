package com.example.fitnessapp.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnessapp.domain.model.Booking

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.history.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SearchBar(
            query = query,
            onQueryChange = viewModel::onQueryChange,
            onSearch = { viewModel.search() },
            onClear = viewModel::clearQuery
        )

        Spacer(Modifier.height(8.dp))

        when (val state = uiState) {
            is SearchUiState.Idle -> {
                if (history.isNotEmpty()) {
                    HistorySection(
                        history = history,
                        onItemClick = viewModel::selectHistoryItem,
                        onClearHistory = viewModel::clearHistory
                    )
                }
            }
            is SearchUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is SearchUiState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.results) { booking ->
                        BookingCard(booking)
                    }
                }
            }
            is SearchUiState.Empty -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Ничего не найдено",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is SearchUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.search() }) {
                            Text("Повторить")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Поиск бронирований...") },
        singleLine = true,
        leadingIcon = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Поиск")
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "Очистить")
                }
            }
        }
    )
}

@Composable
private fun HistorySection(
    history: List<String>,
    onItemClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("История поиска", style = MaterialTheme.typography.titleSmall)
        TextButton(onClick = onClearHistory) {
            Text("Очистить")
        }
    }
    LazyColumn {
        items(history) { item ->
            ListItem(
                headlineContent = { Text(item) },
                leadingContent = {
                    Icon(Icons.Default.History, contentDescription = null)
                },
                modifier = Modifier.clickable { onItemClick(item) }
            )
        }
    }
}

@Composable
private fun BookingCard(booking: Booking) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = booking.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Время: ${booking.time}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Мест: ${booking.slots}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!booking.extra.isNullOrBlank()) {
                Text(
                    text = booking.extra,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
