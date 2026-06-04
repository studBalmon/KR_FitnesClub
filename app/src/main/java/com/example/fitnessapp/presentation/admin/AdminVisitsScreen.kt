package com.example.fitnessapp.presentation.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnessapp.domain.model.InsideVisit
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVisitsScreen(
    viewModel: AdminVisitsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val snackbar by viewModel.snackbar.collectAsState()
    val pendingQuickExit by viewModel.pendingQuickExit.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbar) {
        snackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.snackbarShown()
        }
    }

    pendingQuickExit?.let { prompt ->
        AlertDialog(
            onDismissRequest = viewModel::dismissQuickExit,
            title = { Text("Отметить выход?") },
            text = { Text("${prompt.fio} вошёл менее минуты назад. Возможно, это случайный повторный скан. Всё равно отметить выход?") },
            confirmButton = {
                Button(onClick = viewModel::confirmQuickExit) { Text("Отметить выход") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissQuickExit) { Text("Отмена") }
            }
        )
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        viewModel.onScanned(result.contents)
    }

    fun launchScan() {
        scanLauncher.launch(
            ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Наведите на QR-код пользователя")
                setBeepEnabled(false)
                setOrientationLocked(false)
                setCameraId(0)
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Посещения") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { launchScan() },
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                text = { Text("Сканировать") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            when (val state = uiState) {
                is AdminVisitsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is AdminVisitsUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = viewModel::load) { Text("Повторить") }
                        }
                    }
                }

                is AdminVisitsUiState.Success -> {
                    if (state.inside.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Сейчас внутри никого нет",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Column(Modifier.fillMaxSize()) {
                            Text(
                                "Внутри сейчас: ${state.inside.size}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                            )
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.inside, key = { it.userId }) { VisitRow(it) }
                                item { Spacer(Modifier.height(72.dp)) } // место под FAB
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VisitRow(visit: InsideVisit) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Login,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(visit.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Вошёл в ${visit.entryTime.substringAfter("T").take(5)} · ${formatDuration(visit.minutesInside)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (visit.nextClassName != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Ближайшее: ${visit.nextClassName}" +
                                (visit.nextClassTime?.let { " в $it" } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(minutes: Long): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "$h ч $m мин" else "$m мин"
}
