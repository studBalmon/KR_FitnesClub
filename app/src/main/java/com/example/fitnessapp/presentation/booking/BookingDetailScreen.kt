package com.example.fitnessapp.presentation.booking

import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    onBack: () -> Unit,
    viewModel: BookingDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state is BookingDetailState.Success)
                        Text((state as BookingDetailState.Success).booking.name)
                    else
                        Text("Занятие")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        when (val s = state) {
            is BookingDetailState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is BookingDetailState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(s.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = viewModel::load) { Text("Повторить") }
                    }
                }
            }

            is BookingDetailState.Success -> {
                val b = s.booking
                val embedUrl = remember(b.extra) {
                    b.extra?.let { extractRutubeEmbedUrl(it) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Основная информация ───────────────────────────────────
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailRow(label = "Название", value = b.name)
                            HorizontalDivider()
                            DetailRow(label = "Время", value = b.time.replace("T", " "))
                            HorizontalDivider()
                            DetailRow(
                                label = "Мест",
                                value = "${b.availableSlots} свободно из ${b.slots}"
                            )
                        }
                    }

                    // ── Дополнительная информация + видео ─────────────────────
                    if (!b.extra.isNullOrBlank()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Дополнительно",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // ── Плеер Рутуб (если есть ссылка) ───────────
                                if (embedUrl != null) {
                                    Spacer(Modifier.height(12.dp))
                                    RutubePlayer(
                                        embedUrl = embedUrl,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(210.dp)
                                    )
                                }

                                // ── Текст описания ────────────────────────────
                                Spacer(Modifier.height(8.dp))
                                Text(b.extra, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Встроенный плеер Рутуба через WebView ────────────────────────────────────

@Composable
private fun RutubePlayer(embedUrl: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    allowContentAccess = true
                    allowFileAccess = true
                }
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
                loadUrl(embedUrl)
            }
        },
        update = { webView ->
            // Перезагружаем только если URL изменился
            if (webView.url != embedUrl) webView.loadUrl(embedUrl)
        },
        modifier = modifier
    )
}

// ── Строка деталей ────────────────────────────────────────────────────────────

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
