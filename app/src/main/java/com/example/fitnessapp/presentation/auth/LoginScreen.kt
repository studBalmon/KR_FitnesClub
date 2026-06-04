package com.example.fitnessapp.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val serverSavedMessage by viewModel.serverSavedMessage.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showServerSettings by remember { mutableStateOf(false) }
    var serverInput by remember(serverUrl) { mutableStateOf(serverUrl) }

    LaunchedEffect(Unit) { viewModel.resetState() }

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            viewModel.resetState()
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Вход", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Электронная почта") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (state is AuthState.Error) {
                Text(
                    text = (state as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { viewModel.login(email, password) },
                enabled =
                    state !is AuthState.Loading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Войти")
                }
            }

            TextButton(onClick = onNavigateToRegister) {
                Text("Нет аккаунта? Зарегистрироваться")
            }

            TextButton(onClick = { showServerSettings = !showServerSettings }) {
                Text(if (showServerSettings) "Скрыть настройки сервера" else "Настройки сервера")
            }

            if (showServerSettings) {
                OutlinedTextField(
                    value = serverInput,
                    onValueChange = { serverInput = it },
                    label = { Text("Адрес сервера") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Например: http://192.168.1.197:8080/") }
                )
                Button(
                    onClick = { viewModel.setServerUrl(serverInput) },
                    enabled = serverInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить адрес")
                }
                serverSavedMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
