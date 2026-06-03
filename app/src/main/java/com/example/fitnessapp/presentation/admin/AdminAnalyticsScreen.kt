package com.example.fitnessapp.presentation.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

private val AmberContainer = Color(0xFFFFF3E0)
private val AmberText = Color(0xFFE65100)
private val GreenContainer = Color(0xFFE8F5E9)
private val GreenText = Color(0xFF2E7D32)

private val ANALYTICS_TABS = listOf("Сводка", "Тренеры", "Клиенты")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(
    viewModel: AdminAnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Аналитика") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                ANALYTICS_TABS.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) })
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is AdminAnalyticsUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is AdminAnalyticsUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message, color = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.height(8.dp))
                                Button(onClick = viewModel::load) { Text("Повторить") }
                            }
                        }
                    }

                    is AdminAnalyticsUiState.Success -> when (selectedTab) {
                        0 -> DashboardTab(state.data.dashboard)
                        1 -> CoachesTab(state.data.coaches)
                        else -> ClientsTab(state.data.clients)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardTab(d: DashboardData) {
    TabColumn {
        SectionHeader("Сегодня")
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                Modifier.weight(1f),
                Icons.Default.Event,
                d.classesToday.toString(),
                "Занятий"
            )
            StatCard(
                Modifier.weight(1f),
                Icons.Default.Groups,
                d.enrolledToday.toString(),
                "Записей"
            )
            StatCard(
                Modifier.weight(1f),
                Icons.Default.EventSeat,
                d.freeSlotsToday.toString(),
                "Свободно мест"
            )
        }

        val slotsToday = d.enrolledToday + d.freeSlotsToday
        if (slotsToday > 0) {
            Card(shape = RoundedCornerShape(16.dp)) {
                DonutChart(
                    slices = listOf(
                        ChartSlice(
                            "Занято",
                            d.enrolledToday,
                            MaterialTheme.colorScheme.primary
                        ),
                        ChartSlice(
                            "Свободно",
                            d.freeSlotsToday,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    ),
                    centerValue = "${d.enrolledToday * 100 / slotsToday}%",
                    centerLabel = "занято",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        SectionHeader("Расписание на сегодня")
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                if (d.todaySchedule.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "На сегодня занятий нет",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    d.todaySchedule.forEachIndexed { i, c ->
                        if (i > 0) HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        ScheduleRow(c)
                    }
                }
            }
        }

        SectionHeader("Требуют внимания")
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                Modifier.weight(1f),
                null,
                d.expiringSoon.toString(),
                "Истекают < 7 дн.",
                AmberContainer,
                AmberText
            )
            StatCard(
                Modifier.weight(1f),
                null,
                d.expired.toString(),
                "Истекли",
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer
            )
        }
        if (d.expiringList.isNotEmpty()) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    d.expiringList.forEachIndexed { i, e ->
                        if (i > 0) HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        ExpiringRow(e)
                    }
                }
            }
        }

        SectionHeader("Сводка")
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                Modifier.weight(1f),
                Icons.Default.Person,
                d.totalClients.toString(),
                "Клиентов"
            )
            StatCard(
                Modifier.weight(1f),
                Icons.Default.CardMembership,
                d.activeSubscriptions.toString(),
                "Активных абон."
            )
            StatCard(
                Modifier.weight(1f),
                Icons.Default.FitnessCenter,
                d.totalCoaches.toString(),
                "Тренеров"
            )
        }
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                Modifier.weight(1f),
                Icons.Default.DateRange,
                d.upcomingClasses.toString(),
                "Занятий впереди"
            )
            Spacer(Modifier.weight(2f))
        }

        if (d.loadByDay.any { it.count > 0 }) {
            SectionHeader("Загрузка по дням недели")
            Card(shape = RoundedCornerShape(16.dp)) {
                WeekLoadChart(
                    d.loadByDay, Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ScheduleRow(c: TodayClass) {
    val full = c.enrolled >= c.slots
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            c.time,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.width(48.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                c.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                c.coach,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "${c.enrolled}/${c.slots}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (full) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ExpiringRow(e: ClientExpiring) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            e.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            daysLeftText(e.daysLeft),
            style = MaterialTheme.typography.labelMedium,
            color = AmberText
        )
    }
}

private fun daysLeftText(days: Long): String = when (days) {
    0L -> "сегодня"
    1L -> "завтра"
    else -> "через $days дн."
}


@Composable
private fun CoachesTab(d: CoachesData) {
    TabColumn {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                Modifier.weight(1f),
                Icons.Default.FitnessCenter,
                d.coachCount.toString(),
                "Тренеров"
            )
            StatCard(
                Modifier.weight(1f),
                null,
                "${d.avgFillRate}%",
                "Ср. заполняемость"
            )
            StatCard(
                Modifier.weight(1f),
                Icons.Default.Schedule,
                d.totalHours.toString(), "Часов всего"
            )
        }

        val byType = d.coaches.groupBy { it.coachType ?: "Без типа" }
        if (d.coachCount > 0 && byType.size > 1) {
            SectionHeader("По типам")
            Card(shape = RoundedCornerShape(16.dp)) {
                DonutChart(
                    slices = byType.entries.mapIndexed { i, e ->
                        ChartSlice(
                            e.key,
                            e.value.size,
                            ChartPalette[i % ChartPalette.size]
                        )
                    },
                    centerValue = d.coachCount.toString(),
                    centerLabel = "тренеров",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (d.coaches.isEmpty()) {
            EmptyHint("Нет данных по тренерам")
        } else {
            SectionHeader("Тренеры (${d.coaches.size})")
            d.coaches.forEach { CoachCard(it) }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CoachCard(c: CoachStat) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(
                    c.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (c.coachType != null) {
                    Text(
                        c.coachType,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Заполняемость", style = MaterialTheme.typography.bodySmall)
                Text(
                    "${c.fillRate}%  (${c.enrolled}/${c.slots})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = fillColor(c.fillRate)
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth((c.fillRate / 100f).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(fillColor(c.fillRate))
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Row(Modifier.fillMaxWidth()) {
                MiniStat("Занятий", c.bookings.toString(), Modifier.weight(1f))
                MiniStat("Записей", c.enrolled.toString(), Modifier.weight(1f))
                MiniStat("Часов", c.hours.toString(), Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth()) {
                MiniStat(
                    "Ср. группа",
                    "%.1f".format(c.avgGroup),
                    Modifier.weight(1f)
                )
                MiniStat(
                    "Клиентов",
                    c.uniqueClients.toString(),
                    Modifier.weight(1f)
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ClientsTab(d: ClientsData) {
    TabColumn {
        SectionHeader("Абонементы")
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                Modifier.weight(1f),
                null,
                d.active.toString(),
                "Активных",
                GreenContainer,
                GreenText
            )
            StatCard(
                Modifier.weight(1f),
                null,
                d.expiringSoon.toString(),
                "Истекают < 7 дн.",
                AmberContainer,
                AmberText
            )
            StatCard(
                Modifier.weight(1f),
                null,
                d.expired.toString(),
                "Истекли",
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer
            )
        }
        Card(shape = RoundedCornerShape(16.dp)) {
            DonutChart(
                slices = listOf(
                    ChartSlice(
                        "Активные",
                        d.active,
                        GreenText
                    ),
                    ChartSlice(
                        "Истекают < 7 дн.",
                        d.expiringSoon,
                        AmberText
                    ),
                    ChartSlice(
                        "Истекли",
                        d.expired,
                        MaterialTheme.colorScheme.error
                    )
                ),
                centerValue = d.totalClients.toString(),
                centerLabel = "клиентов",
                modifier = Modifier.padding(16.dp)
            )
        }

        SectionHeader("Вовлечённость")
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                Modifier.weight(1f),
                Icons.Default.HowToReg,
                d.withBookings.toString(),
                "С записями"
            )
            StatCard(
                Modifier.weight(1f),
                Icons.Default.PersonOff,
                d.withoutBookings.toString(),
                "Без записей"
            )
            StatCard(
                Modifier.weight(1f),
                Icons.Default.Person,
                d.totalClients.toString(),
                "Всего"
            )
        }
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                Modifier.weight(1f),
                null,
                "%.1f".format(d.avgBookingsPerClient),
                "Записей/клиент"
            )
            StatCard(
                Modifier.weight(1f),
                null,
                "%.1f".format(d.avgHoursPerActive),
                "Часов/клиент"
            )
            StatCard(
                Modifier.weight(1f),
                Icons.Default.Schedule,
                d.totalHours.toString(),
                "Часов всего"
            )
        }

        if (d.freq.any { it.count > 0 }) {
            SectionHeader("Частота посещений")
            Card(shape = RoundedCornerShape(16.dp)) {
                DonutChart(
                    slices = d.freq.mapIndexed { i, b ->
                        ChartSlice(
                            b.label, b.count,
                            ChartPalette[i % ChartPalette.size]
                        )
                    },
                    centerValue = d.totalClients.toString(),
                    centerLabel = "клиентов",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (d.topByVisits.isNotEmpty()) {
            SectionHeader("Топ по записям")
            val maxV = d.topByVisits.maxOf { it.visits }.coerceAtLeast(1)
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    d.topByVisits.forEach {
                        LabeledBar(
                            it.name, "${it.visits} зап.",
                            it.visits.toFloat() / maxV
                        )
                    }
                }
            }
        }

        if (d.topByHours.isNotEmpty()) {
            SectionHeader("Топ по часам")
            val maxH = d.topByHours.maxOf { it.hours }.coerceAtLeast(0.1)
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    d.topByHours.forEach {
                        LabeledBar(
                            it.name, "%.1f ч".format(it.hours),
                            (it.hours / maxH).toFloat()
                        )
                    }
                }
            }
        }

        if (d.topWorkouts.isNotEmpty()) {
            SectionHeader("Популярные занятия")
            val maxE = d.topWorkouts.maxOf { it.enrolled }.coerceAtLeast(1)
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    d.topWorkouts.forEach {
                        LabeledBar(
                            it.name,
                            "${it.enrolled} зап. · ${it.bookings} зан.",
                            it.enrolled.toFloat() / maxE
                        )
                    }
                }
            }
        }

        if (d.expiringList.isNotEmpty()) {
            SectionHeader("Скоро истекают (${d.expiringList.size})")
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    d.expiringList.forEachIndexed { i, e ->
                        if (i > 0) HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        ExpiringRow(e)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TabColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(24.dp), contentAlignment = Alignment.Center
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    value: String,
    label: String,
    container: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    content: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = content,
                maxLines = 1
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = content.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LabeledBar(
    label: String, value: String, fraction: Float, color: Color = MaterialTheme.colorScheme.primary
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun WeekLoadChart(days: List<DayLoad>, modifier: Modifier = Modifier) {
    val maxCount = days.maxOf { it.count }.coerceAtLeast(1)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    day.count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (day.count > 0) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.height(110.dp)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(day.count.toFloat() / maxCount)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (day.count > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
                Text(day.label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun fillColor(rate: Int): Color = when {
    rate >= 80 -> GreenText
    rate >= 40 -> MaterialTheme.colorScheme.primary
    else -> AmberText
}

private val ChartPalette = listOf(
    Color(0xFF42A5F5),
    Color(0xFF66BB6A),
    Color(0xFFFFA726),
    Color(0xFFAB47BC),
    Color(0xFFEF5350),
    Color(0xFF26C6DA)
)

data class ChartSlice(val label: String, val value: Int, val color: Color)

@Composable
private fun DonutChart(
    slices: List<ChartSlice>,
    modifier: Modifier = Modifier,
    centerValue: String? = null,
    centerLabel: String? = null
) {
    val total = slices.sumOf { it.value }
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(132.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                val strokeW = 26.dp.toPx()
                val diameter = size.minDimension - strokeW
                val topLeft =
                    Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)
                if (total <= 0) {
                    drawArc(
                        emptyColor,
                        0f,
                        360f,
                        false,
                        topLeft,
                        arcSize,
                        style = Stroke(strokeW)
                    )
                } else {
                    var start = -90f
                    slices.forEach { s ->
                        if (s.value > 0) {
                            val sweep = s.value / total.toFloat() * 360f
                            drawArc(
                                color = s.color,
                                startAngle = start,
                                sweepAngle = (sweep - 1.5f).coerceAtLeast(0.5f),
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(strokeW)
                            )
                            start += sweep
                        }
                    }
                }
            }
            if (centerValue != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        centerValue,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (centerLabel != null) {
                        Text(
                            centerLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            slices.forEach { s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(s.color)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        s.label,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        s.value.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
