package com.example.fitnessapp.presentation.booking

private val RUTUBE_PATTERNS = listOf(
    Regex("""rutube\.ru/video/([a-zA-Z0-9]+)"""),
    Regex("""rutube\.ru/play/embed/([a-zA-Z0-9]+)""")
)

fun extractRutubeEmbedUrl(text: String): String? {
    for (pattern in RUTUBE_PATTERNS) {
        val match = pattern.find(text) ?: continue
        val videoId = match.groupValues[1]
        if (videoId.isNotEmpty()) return "https://rutube.ru/play/embed/$videoId"
    }
    return null
}
