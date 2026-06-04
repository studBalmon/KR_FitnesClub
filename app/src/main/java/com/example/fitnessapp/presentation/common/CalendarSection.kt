package com.example.fitnessapp.presentation.common

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle as ComposeTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private const val DAYS_BACK = 7
private const val DAYS_FORWARD = 60
private const val SWIPE_THRESHOLD = 40f

@Composable
fun CalendarSection(
    selectedDate: LocalDate,
    bookingCounts: Map<LocalDate, Int>,
    onDateSelected: (LocalDate) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var displayedMonth by remember {
        mutableStateOf(
            YearMonth.from(selectedDate)
        )
    }

    LaunchedEffect(selectedDate) {
        displayedMonth = YearMonth.from(selectedDate)
    }

    var dragAccum by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
            .pointerInput(expanded) {
                detectVerticalDragGestures(
                    onDragStart = { dragAccum = 0f },
                    onDragEnd = {
                        if (!expanded && dragAccum > SWIPE_THRESHOLD) expanded = true
                        if (expanded && dragAccum < -SWIPE_THRESHOLD) expanded = false
                        dragAccum = 0f
                    },
                    onVerticalDrag = { change, delta ->
                        dragAccum += delta
                        change.consume()
                    }
                )
            }
    ) {
        if (expanded) {
            MonthCalendar(
                selectedDate = selectedDate,
                displayedMonth = displayedMonth,
                bookingCounts = bookingCounts,
                onDateSelected = {
                    onDateSelected(it)
                    displayedMonth = YearMonth.from(it)
                },
                onMonthChange = { displayedMonth = it }
            )
        } else {
            DateStrip(
                selectedDate = selectedDate,
                bookingCounts = bookingCounts,
                onDateSelected = onDateSelected
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Свернуть" else "Развернуть",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    HorizontalDivider()
}

@Composable
private fun DateStrip(
    selectedDate: LocalDate,
    bookingCounts: Map<LocalDate, Int>,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    val dates = remember {
        (-DAYS_BACK..DAYS_FORWARD).map {
            today.plusDays(it.toLong())
        }
    }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem((DAYS_BACK - 3).coerceAtLeast(0))
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(dates, key = { it.toEpochDay() }) { date ->
            DateChip(
                date = date,
                isSelected = date == selectedDate,
                isToday = date == today,
                bookingCount = bookingCounts[date] ?: 0,
                onSelected = { onDateSelected(date) }
            )
        }
    }
}

@Composable
private fun DateChip(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    bookingCount: Int,
    onSelected: () -> Unit
) {
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val badgeBg = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
        else -> MaterialTheme.colorScheme.primary
    }
    val badgeText = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onPrimary
    }

    val dayName = date.dayOfWeek
        .getDisplayName(TextStyle.SHORT, Locale("ru"))
        .replaceFirstChar { it.uppercaseChar() }.take(2)

    val monthLabel = if (date.dayOfMonth == 1)
        date.month.getDisplayName(TextStyle.SHORT, Locale("ru"))
            .replaceFirstChar { it.uppercaseChar() }
    else null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onSelected)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .widthIn(min = 40.dp)
    ) {
        Text(
            dayName, style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.75f), textAlign = TextAlign.Center
        )
        Text(
            date.dayOfMonth.toString(), style = MaterialTheme.typography.titleSmall,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor, textAlign = TextAlign.Center
        )
        val badgeLabel = when {
            bookingCount in 1..9 -> bookingCount.toString()
            bookingCount >= 10 -> "9+"
            else -> null
        }
        if (badgeLabel != null) {
            val isCapsule = bookingCount >= 10
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .then(
                        if (isCapsule) Modifier
                            .height(16.dp)
                            .widthIn(min = 24.dp)
                        else Modifier.size(16.dp)
                    )
                    .clip(RoundedCornerShape(50))
                    .background(badgeBg)
                    .padding(horizontal = if (isCapsule) 4.dp else 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    badgeLabel,
                    style = ComposeTextStyle(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeight = 9.sp
                    ),
                    color = badgeText,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Spacer(Modifier.height(19.dp))
        }
    }
}

@Composable
private fun MonthCalendar(
    selectedDate: LocalDate,
    displayedMonth: YearMonth,
    bookingCounts: Map<LocalDate, Int>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    val today = remember { LocalDate.now() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { onMonthChange(displayedMonth.minusMonths(1)) }) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "Предыдущий месяц"
            )
        }
        val monthName = displayedMonth.month
            .getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
            .replaceFirstChar { it.uppercaseChar() }
        Text(
            "$monthName ${displayedMonth.year}",
            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = { onMonthChange(displayedMonth.plusMonths(1)) }) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Следующий месяц"
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
            Text(
                day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }

    Spacer(Modifier.height(4.dp))

    val calendarDays = remember(displayedMonth) {
        buildCalendarDays(displayedMonth)
    }
    calendarDays.chunked(7).forEach { week ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            week.forEach { date ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (date != null) {
                        CalendarDayCell(
                            date = date,
                            isSelected = date == selectedDate,
                            isToday = date == today,
                            isCurrentMonth = YearMonth.from(date) == displayedMonth,
                            bookingCount = bookingCounts[date] ?: 0,
                            onClick = { onDateSelected(date) }
                        )
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(4.dp))
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    bookingCount: Int,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val badgeBg = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick)
    ) {
        Text(
            date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor, textAlign = TextAlign.Center
        )
        val badgeLabel = when {
            bookingCount in 1..9 && isCurrentMonth -> bookingCount.toString()
            bookingCount >= 10 && isCurrentMonth -> "9+"
            else -> null
        }
        if (badgeLabel != null) {
            val isCapsule = bookingCount >= 10
            Box(
                modifier = Modifier
                    .then(
                        if (isCapsule) Modifier
                            .height(12.dp)
                            .widthIn(min = 18.dp)
                        else Modifier.size(12.dp)
                    )
                    .clip(RoundedCornerShape(50))
                    .background(badgeBg)
                    .padding(horizontal = if (isCapsule) 3.dp else 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    badgeLabel,
                    style = ComposeTextStyle(
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeight = 7.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Spacer(Modifier.height(12.dp))
        }
    }
}

fun buildCalendarDays(yearMonth: YearMonth): List<LocalDate?> {
    val firstDay = yearMonth.atDay(1)
    val leadingEmpties = firstDay.dayOfWeek.value - 1
    val result = mutableListOf<LocalDate?>()
    repeat(leadingEmpties) { result.add(null) }
    for (d in 1..yearMonth.lengthOfMonth()) result.add(yearMonth.atDay(d))
    while (result.size % 7 != 0) result.add(null)
    return result
}
