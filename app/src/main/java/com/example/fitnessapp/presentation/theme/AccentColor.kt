package com.example.fitnessapp.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class AccentColor(
    val key: String,
    val labelRu: String,
    val previewColor: Color,
    val light: ColorScheme,
    val dark: ColorScheme
) {
    Purple(
        key = "purple",
        labelRu = "Фиолетовый",
        previewColor = Color(0xFF6750A4),
        light = lightColorScheme(
            primary = Color(0xFF6750A4),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFEADDFF),
            onPrimaryContainer = Color(0xFF21005D),
            secondary = Color(0xFF625B71),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE8DEF8),
            onSecondaryContainer = Color(0xFF1D192B),
        ),
        dark = darkColorScheme(
            primary = Color(0xFFD0BCFF),
            onPrimary = Color(0xFF381E72),
            primaryContainer = Color(0xFF4F378B),
            onPrimaryContainer = Color(0xFFEADDFF),
            secondary = Color(0xFFCCC2DC),
            onSecondary = Color(0xFF332D41),
            secondaryContainer = Color(0xFF4A4458),
            onSecondaryContainer = Color(0xFFE8DEF8),
        )
    ),
    Blue(
        key = "blue",
        labelRu = "Синий",
        previewColor = Color(0xFF1565C0),
        light = lightColorScheme(
            primary = Color(0xFF1565C0),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD6E4FF),
            onPrimaryContainer = Color(0xFF001849),
            secondary = Color(0xFF545F71),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFD8E3F8),
            onSecondaryContainer = Color(0xFF101C2B),
        ),
        dark = darkColorScheme(
            primary = Color(0xFFADC6FF),
            onPrimary = Color(0xFF002D6E),
            primaryContainer = Color(0xFF004199),
            onPrimaryContainer = Color(0xFFD6E4FF),
            secondary = Color(0xFFBCC7DB),
            onSecondary = Color(0xFF263141),
            secondaryContainer = Color(0xFF3C4858),
            onSecondaryContainer = Color(0xFFD8E3F8),
        )
    ),
    Teal(
        key = "teal",
        labelRu = "Бирюзовый",
        previewColor = Color(0xFF006A6A),
        light = lightColorScheme(
            primary = Color(0xFF006A6A),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF9CF1F1),
            onPrimaryContainer = Color(0xFF002020),
            secondary = Color(0xFF4A6363),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFCCE8E8),
            onSecondaryContainer = Color(0xFF051F1F),
        ),
        dark = darkColorScheme(
            primary = Color(0xFF80D5D5),
            onPrimary = Color(0xFF003737),
            primaryContainer = Color(0xFF004F4F),
            onPrimaryContainer = Color(0xFF9CF1F1),
            secondary = Color(0xFFB0CCCC),
            onSecondary = Color(0xFF1B3535),
            secondaryContainer = Color(0xFF324B4B),
            onSecondaryContainer = Color(0xFFCCE8E8),
        )
    ),
    Green(
        key = "green",
        labelRu = "Зелёный",
        previewColor = Color(0xFF2E7D32),
        light = lightColorScheme(
            primary = Color(0xFF306A2E),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFB1F0AC),
            onPrimaryContainer = Color(0xFF002106),
            secondary = Color(0xFF526350),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFD4E8D0),
            onSecondaryContainer = Color(0xFF0F1F10),
        ),
        dark = darkColorScheme(
            primary = Color(0xFF96D492),
            onPrimary = Color(0xFF003909),
            primaryContainer = Color(0xFF185218),
            onPrimaryContainer = Color(0xFFB1F0AC),
            secondary = Color(0xFFB8CCB5),
            onSecondary = Color(0xFF243524),
            secondaryContainer = Color(0xFF3A4C3A),
            onSecondaryContainer = Color(0xFFD4E8D0),
        )
    ),
    Orange(
        key = "orange",
        labelRu = "Оранжевый",
        previewColor = Color(0xFFBF360C),
        light = lightColorScheme(
            primary = Color(0xFF8B5000),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFDDB6),
            onPrimaryContainer = Color(0xFF2C1600),
            secondary = Color(0xFF725B42),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFDDBF),
            onSecondaryContainer = Color(0xFF281806),
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB95D),
            onPrimary = Color(0xFF4A2800),
            primaryContainer = Color(0xFF693C00),
            onPrimaryContainer = Color(0xFFFFDDB6),
            secondary = Color(0xFFE2BFA0),
            onSecondary = Color(0xFF412D17),
            secondaryContainer = Color(0xFF5A432B),
            onSecondaryContainer = Color(0xFFFFDDBF),
        )
    ),
    Red(
        key = "red",
        labelRu = "Красный",
        previewColor = Color(0xFFC62828),
        light = lightColorScheme(
            primary = Color(0xFFB91C1C),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFDAD6),
            onPrimaryContainer = Color(0xFF410002),
            secondary = Color(0xFF775652),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFDAD6),
            onSecondaryContainer = Color(0xFF2C1512),
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB4AB),
            onPrimary = Color(0xFF690005),
            primaryContainer = Color(0xFF93000A),
            onPrimaryContainer = Color(0xFFFFDAD6),
            secondary = Color(0xFFE7BDB8),
            onSecondary = Color(0xFF442926),
            secondaryContainer = Color(0xFF5D3F3C),
            onSecondaryContainer = Color(0xFFFFDAD6),
        )
    );

    companion object {
        val default = Purple
        fun fromKey(key: String) = entries.find { it.key == key } ?: default
    }
}
