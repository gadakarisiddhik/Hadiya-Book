package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HadiyaRecord
import com.example.ui.components.DatePickerModal
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MutedGold
import com.example.ui.viewmodel.HadiyaViewModel
import com.example.ui.viewmodel.ReportPeriod
import com.example.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: HadiyaViewModel,
    onOpenAddHadiya: () -> Unit,
    onViewRecord: (HadiyaRecord) -> Unit
) {
    val allRecords by viewModel.allRecords.collectAsState()
    val currentPeriod by viewModel.reportPeriod.collectAsState()
    val selectedDate by viewModel.selectedReportDate.collectAsState()
    val selectedYear by viewModel.selectedReportYear.collectAsState()
    val selectedMonth by viewModel.selectedReportMonth.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerModal(
            initialIsoDate = selectedDate,
            onDateSelected = {
                viewModel.selectedReportDate.value = it
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    val d = sdf.parse(it)
                    if (d != null) {
                        val cal = Calendar.getInstance().apply { time = d }
                        viewModel.selectedReportYear.value = cal.get(Calendar.YEAR)
                        viewModel.selectedReportMonth.value = cal.get(Calendar.MONTH) + 1
                    }
                } catch (_: Exception) {}
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Filter records according to selected period
    val periodRecords = remember(allRecords, currentPeriod, selectedDate, selectedYear, selectedMonth) {
        when (currentPeriod) {
            ReportPeriod.DAILY -> allRecords.filter { it.date == selectedDate }
            ReportPeriod.MONTHLY -> {
                val prefix = DateUtils.getMonthPrefix(selectedYear, selectedMonth)
                allRecords.filter { it.date.startsWith(prefix) }
            }
            ReportPeriod.YEARLY -> {
                val prefix = selectedYear.toString()
                allRecords.filter { it.date.startsWith(prefix) }
            }
        }
    }

    val totalAmount = remember(periodRecords) { periodRecords.sumOf { it.amount } }
    val count = periodRecords.size
    val averageAmount = remember(periodRecords) { if (count > 0) totalAmount / count else 0L }
    val maxAmount = remember(periodRecords) { periodRecords.maxOfOrNull { it.amount } ?: 0L }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Tab Selector (Daily, Monthly, Yearly) ---
        item {
            PrimaryTabRow(
                selectedTabIndex = currentPeriod.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EmeraldGreen
            ) {
                Tab(
                    selected = currentPeriod == ReportPeriod.DAILY,
                    onClick = { viewModel.reportPeriod.value = ReportPeriod.DAILY },
                    text = { Text("Daily Report", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = currentPeriod == ReportPeriod.MONTHLY,
                    onClick = { viewModel.reportPeriod.value = ReportPeriod.MONTHLY },
                    text = { Text("Monthly Report", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = currentPeriod == ReportPeriod.YEARLY,
                    onClick = { viewModel.reportPeriod.value = ReportPeriod.YEARLY },
                    text = { Text("Yearly Report", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        // --- 2. Period Navigation Controller ---
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    IconButton(
                        onClick = {
                            when (currentPeriod) {
                                ReportPeriod.DAILY -> {
                                    val cal = Calendar.getInstance()
                                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                                    val d = sdf.parse(selectedDate) ?: Date()
                                    cal.time = d
                                    cal.add(Calendar.DAY_OF_MONTH, -1)
                                    viewModel.selectedReportDate.value = sdf.format(cal.time)
                                }
                                ReportPeriod.MONTHLY -> {
                                    if (selectedMonth == 1) {
                                        viewModel.selectedReportMonth.value = 12
                                        viewModel.selectedReportYear.value = selectedYear - 1
                                    } else {
                                        viewModel.selectedReportMonth.value = selectedMonth - 1
                                    }
                                }
                                ReportPeriod.YEARLY -> {
                                    viewModel.selectedReportYear.value = selectedYear - 1
                                }
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                    }

                    // Center Date Label / Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = when (currentPeriod) {
                                ReportPeriod.DAILY -> DateUtils.formatToDisplay(selectedDate)
                                ReportPeriod.MONTHLY -> DateUtils.formatMonthYear(selectedYear, selectedMonth)
                                ReportPeriod.YEARLY -> "Year $selectedYear"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentPeriod == ReportPeriod.DAILY) {
                            IconButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Pick Date",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Next Button
                    IconButton(
                        onClick = {
                            when (currentPeriod) {
                                ReportPeriod.DAILY -> {
                                    val cal = Calendar.getInstance()
                                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                                    val d = sdf.parse(selectedDate) ?: Date()
                                    cal.time = d
                                    cal.add(Calendar.DAY_OF_MONTH, 1)
                                    viewModel.selectedReportDate.value = sdf.format(cal.time)
                                }
                                ReportPeriod.MONTHLY -> {
                                    if (selectedMonth == 12) {
                                        viewModel.selectedReportMonth.value = 1
                                        viewModel.selectedReportYear.value = selectedYear + 1
                                    } else {
                                        viewModel.selectedReportMonth.value = selectedMonth + 1
                                    }
                                }
                                ReportPeriod.YEARLY -> {
                                    viewModel.selectedReportYear.value = selectedYear + 1
                                }
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                    }
                }
            }
        }

        // --- 3. Summary Metric Cards ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Amount
                ReportSummaryTile(
                    title = "TOTAL HADIYA",
                    value = DateUtils.formatCurrency(totalAmount),
                    subtitle = "$count ${if (count == 1) "entry" else "entries"}",
                    accentColor = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )

                // Average Amount
                ReportSummaryTile(
                    title = "AVERAGE",
                    value = DateUtils.formatCurrency(averageAmount),
                    subtitle = "Highest: ${DateUtils.formatCurrency(maxAmount)}",
                    accentColor = MutedGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- 4. Lightweight Visual Collection Summary ---
        if (currentPeriod == ReportPeriod.YEARLY) {
            item {
                YearlyMonthlyBarChart(
                    records = allRecords,
                    year = selectedYear
                )
            }
        } else if (currentPeriod == ReportPeriod.MONTHLY && periodRecords.isNotEmpty()) {
            item {
                MonthlyDaysBarChart(
                    records = periodRecords,
                    year = selectedYear,
                    month = selectedMonth
                )
            }
        }

        // --- 5. Record Entries for Selected Period ---
        item {
            Text(
                text = "Entries in this period (${periodRecords.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (periodRecords.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No data available for this period.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No Hadiya donations recorded for the selected timeline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onOpenAddHadiya,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Hadiya")
                        }
                    }
                }
            }
        } else {
            items(periodRecords, key = { it.id }) { record ->
                Card(
                    onClick = { onViewRecord(record) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = record.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${record.phone} • ${DateUtils.formatToDisplay(record.date)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!record.note.isNullOrBlank()) {
                                Text(
                                    text = record.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedGold
                                )
                            }
                        }
                        Text(
                            text = DateUtils.formatCurrency(record.amount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreen
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ReportSummaryTile(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Clean, lightweight 12-month bar chart for Yearly Report
 */
@Composable
fun YearlyMonthlyBarChart(
    records: List<HadiyaRecord>,
    year: Int
) {
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val monthlyTotals = remember(records, year) {
        (1..12).map { month ->
            val prefix = DateUtils.getMonthPrefix(year, month)
            records.filter { it.date.startsWith(prefix) }.sumOf { it.amount }
        }
    }
    val maxMonthAmount = remember(monthlyTotals) { (monthlyTotals.maxOrNull() ?: 1L).coerceAtLeast(1L) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.BarChart, contentDescription = null, tint = EmeraldGreen)
                Text(
                    text = "Monthly Collection Breakdown ($year)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bars container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                monthNames.forEachIndexed { index, name ->
                    val amount = monthlyTotals[index]
                    val fraction = (amount.toFloat() / maxMonthAmount.toFloat()).coerceIn(0.04f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (amount > 0) EmeraldGreen else MaterialTheme.colorScheme.outlineVariant)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = name,
                            fontSize = 10.sp,
                            fontWeight = if (amount > 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (amount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean, lightweight 4-week collection summary for Monthly Report
 */
@Composable
fun MonthlyDaysBarChart(
    records: List<HadiyaRecord>,
    year: Int,
    month: Int
) {
    val weekBuckets = remember(records) {
        val w1 = records.filter {
            val day = it.date.takeLast(2).toIntOrNull() ?: 0
            day in 1..7
        }.sumOf { it.amount }

        val w2 = records.filter {
            val day = it.date.takeLast(2).toIntOrNull() ?: 0
            day in 8..14
        }.sumOf { it.amount }

        val w3 = records.filter {
            val day = it.date.takeLast(2).toIntOrNull() ?: 0
            day in 15..21
        }.sumOf { it.amount }

        val w4 = records.filter {
            val day = it.date.takeLast(2).toIntOrNull() ?: 0
            day >= 22
        }.sumOf { it.amount }

        listOf("Day 1-7" to w1, "Day 8-14" to w2, "Day 15-21" to w3, "Day 22-31" to w4)
    }

    val maxVal = remember(weekBuckets) { (weekBuckets.maxOfOrNull { it.second } ?: 1L).coerceAtLeast(1L) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Collection by Month Period",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            weekBuckets.forEach { (label, amt) ->
                val progress = (amt.toFloat() / maxVal.toFloat()).coerceIn(0.02f, 1f)
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = DateUtils.formatCurrency(amt),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldGreen)
                        )
                    }
                }
            }
        }
    }
}
