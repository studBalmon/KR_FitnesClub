package com.example.fitnessapp.presentation.pass

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.repository.UserRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PassState(
    val token: String? = null,
    val fio: String = "",
    val loading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class QrViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PassState())
    val state: StateFlow<PassState> = _state

    init {
        load()

        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(120_000)
                userRepository.getPassToken().onSuccess {
                    _state.value = _state.value.copy(token = it.token, fio = it.fio)
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            userRepository.getPassToken()
                .onSuccess { _state.value = PassState(token = it.token, fio = it.fio, loading = false) }
                .onFailure { _state.value = PassState(loading = false, error = it.message ?: "Ошибка загрузки") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrPassScreen(viewModel: QrViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Пропуск") }) }) { padding ->
        Box(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.loading -> CircularProgressIndicator()

                state.error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::load) { Text("Повторить") }
                }

                state.token != null -> {
                    val token = state.token!!
                    val qr = remember(token) { generateQr(token, 600) }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(state.fio, style = MaterialTheme.typography.titleMedium)
                        if (qr != null) {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp),
                                tonalElevation = 2.dp
                            ) {
                                Image(
                                    bitmap = qr.asImageBitmap(),
                                    contentDescription = "QR-код пропуска",
                                    modifier = Modifier.padding(16.dp).size(260.dp)
                                )
                            }
                        }
                        Text(
                            "Покажите этот QR-код администратору на входе и выходе из клуба",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun generateQr(content: String, size: Int): Bitmap? = runCatching {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    bmp
}.getOrNull()
