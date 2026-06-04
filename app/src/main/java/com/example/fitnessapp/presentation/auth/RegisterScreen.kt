package com.example.fitnessapp.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

private fun validateFio(v: String): String? {
    if (v.isBlank()) return "Введите ФИО"
    val parts = v.trim().split(Regex("\\s+"))
    if (parts.size < 2) return "Введите фамилию и имя"
    if (!v.all { it.isLetter() || it.isWhitespace() }) return "ФИО должно содержать только буквы"
    return null
}

private fun validatePhone(v: String): String? {
    if (v.isBlank()) return "Введите номер телефона"
    val digits = v.filter { it.isDigit() }
    if (digits.length != 11) return "Телефон должен содержать 11 цифр"
    if (!digits.startsWith("7") && !digits.startsWith("8")) return "Телефон должен начинаться с +7 или 8"
    return null
}

private fun validateEmail(v: String): String? {
    if (v.isBlank()) return "Введите электронную почту"
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    if (!emailRegex.matches(v.trim())) return "Некорректный формат почты"
    return null
}

private fun validatePassword(v: String): String? {
    if (v.isBlank()) return "Введите пароль"
    if (v.length < 6) return "Пароль должен содержать минимум 6 символов"
    return null
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var fio      by remember { mutableStateOf("") }
    var phone    by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var submitted by remember { mutableStateOf(false) }

    val fioError      = if (submitted) validateFio(fio)      else null
    val phoneError    = if (submitted) validatePhone(phone)    else null
    val emailError    = if (submitted) validateEmail(email)    else null
    val passwordError = if (submitted) validatePassword(password) else null

    LaunchedEffect(Unit) { viewModel.resetState() }

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            viewModel.resetState()
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(32.dp))

        Text(text = "Регистрация", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = fio,
            onValueChange = { fio = it },
            label = { Text("ФИО") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = fioError != null,
            supportingText = fioError?.let { { Text(it) } }
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Телефон") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = phoneError != null,
            supportingText = phoneError?.let { { Text(it) } },
            placeholder = { Text("+7 (999) 000-00-00") }
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Электронная почта") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } }
        )

        if (state is AuthState.Error) {
            Text(
                text = (state as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                submitted = true
                val valid = validateFio(fio) == null &&
                        validatePhone(phone) == null &&
                        validateEmail(email) == null &&
                        validatePassword(password) == null
                if (valid) viewModel.register(fio.trim(), phone.trim(), email.trim(), password)
            },
            enabled = state !is AuthState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Зарегистрироваться")
            }
        }

        TextButton(onClick = onNavigateToLogin) {
            Text("Уже есть аккаунт? Войти")
        }
    }
}
