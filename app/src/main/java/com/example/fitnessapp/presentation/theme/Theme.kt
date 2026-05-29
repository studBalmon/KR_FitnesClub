package com.example.fitnessapp.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun FitnessAppTheme(
    darkTheme: Boolean = false,
    accent: AccentColor = AccentColor.default,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) accent.dark else accent.light,
        content = content
    )
}
