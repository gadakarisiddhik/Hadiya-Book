package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HadiyaRecord
import com.example.ui.components.DatePickerModal
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MutedGold
import com.example.ui.viewmodel.HadiyaViewModel
import com.example.ui.viewmodel.HistoryFilter
import com.example.ui.viewmodel.HistorySort
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper

@Composable
fun HistoryScreen(
    viewModel: HadiyaViewModel,
    onOpenAddHadiya: () -> Unit,
    onViewRecord: (HadiyaRecord) -> Unit,
    onEditRecord: (HadiyaRecord) -> Unit,
    onDeleteRecord: (HadiyaRecord) -> Unit
) {
    val context = LocalContext.current
    val records by viewModel.filteredRecords.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.historyFilter.collectAsState()
    val currentSort by viewModel.historySort.collectAsState()
    val customStartDate by viewModel.customStartDate.collectAsState()
    val customEndDate by viewModel.customEndDate.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showCustomRangePicker by remember { mutableStateOf(false) }
    var pickingStartDate by remember { mutableStateOf(true) }

    val totalAmount = remember(records) { records.sumOf { it.amount } }

    if (showCustomRangePicker) {
        DatePickerModal(
            initialIsoDate = if (pickingStartDate) customStartDate else customEndDate,
            onDateSelected = { date ->
                if (pickingStartDate) {
                    viewModel.customStartDate.value = date
                    pickingStartDate = false
                } else {
                    viewModel.customEndDate.value = date
                    showCustomRangePicker = false
                    pickingStartDate = true
                    viewModel.historyFilter.value = HistoryFilter.CUSTOM_RANGE
                }
            },
            onDismiss = {
                showCustomRangePicker = false
                pickingStartDate = true
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("history_screen")
    ) {
        // --- 1. Search Bar ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search by name or phone...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = EmeraldGreen
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = currentFilter == HistoryFilter.ALL,
                        onClick = { viewModel.historyFilter.value = HistoryFilter.ALL },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                    FilterChip(
                        selected = currentFilter == HistoryFilter.TODAY,
                        onClick = { viewModel.historyFilter.value = HistoryFilter.TODAY },
                        label = { Text("Today") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                    FilterChip(
                        selected = currentFilter == HistoryFilter.THIS_WEEK,
                        onClick = { viewModel.historyFilter.value = HistoryFilter.THIS_WEEK },
                        label = { Text("This Week") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                    FilterChip(
                        selected = currentFilter == HistoryFilter.THIS_MONTH,
                        onClick = { viewModel.historyFilter.value = HistoryFilter.THIS_MONTH },
                        label = { Text("This Month") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                    FilterChip(
                        selected = currentFilter == HistoryFilter.CUSTOM_RANGE,
                        onClick = {
                            pickingStartDate = true
                            showCustomRangePicker = true
                        },
                        label = {
                            Text(
                                if (currentFilter == HistoryFilter.CUSTOM_RANGE)
                                    "${DateUtils.formatToShortDisplay(customStartDate)} - ${DateUtils.formatToShortDisplay(customEndDate)}"
                                else
                                    "Custom Range"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }
        }

        // --- 2. Filter Summary & Sort Bar ---
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${records.size} ${if (records.size == 1) "Record" else "Records"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total: ${DateUtils.formatCurrency(totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }

                Box {
                    TextButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentSort) {
                                HistorySort.NEWEST_FIRST -> "Newest"
                                HistorySort.OLDEST_FIRST -> "Oldest"
                                HistorySort.HIGHEST_AMOUNT -> "Highest"
                                HistorySort.LOWEST_AMOUNT -> "Lowest"
                            },
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Newest first") },
                            onClick = {
                                viewModel.historySort.value = HistorySort.NEWEST_FIRST
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Oldest first") },
                            onClick = {
                                viewModel.historySort.value = HistorySort.OLDEST_FIRST
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Highest amount") },
                            onClick = {
                                viewModel.historySort.value = HistorySort.HIGHEST_AMOUNT
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lowest amount") },
                            onClick = {
                                viewModel.historySort.value = HistorySort.LOWEST_AMOUNT
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        // --- 3. Records List or Empty State ---
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Hadiya records yet.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty() || currentFilter != HistoryFilter.ALL)
                            "No records match your search or filter criteria."
                        else
                            "Start recording donations given to the Masjid.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onOpenAddHadiya,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("empty_add_hadiya_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Hadiya")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    HistoryRecordCard(
                        record = record,
                        onCardClick = { onViewRecord(record) },
                        onEdit = { onEditRecord(record) },
                        onDelete = { onDeleteRecord(record) },
                        onWhatsApp = {
                            val msg = WhatsAppHelper.buildHadiyaMessage(
                                name = record.name,
                                amount = record.amount,
                                dateIso = record.date
                            )
                            WhatsAppHelper.openWhatsApp(context, record.phone, msg)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryRecordCard(
    record: HadiyaRecord,
    onCardClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onWhatsApp: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onCardClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name and Phone
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = record.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Amount Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldGreen.copy(alpha = 0.12f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = DateUtils.formatCurrency(record.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Menu button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Actions",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Details") },
                            leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onCardClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Send WhatsApp") },
                            leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, tint = EmeraldGreen) },
                            onClick = {
                                showMenu = false
                                onWhatsApp()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Record") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Date and Note Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatToDisplay(record.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                if (!record.note.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MutedGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = record.note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedGold,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .widthIn(max = 160.dp)
                        )
                    }
                }

                // Quick WhatsApp Icon
                IconButton(
                    onClick = onWhatsApp,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "WhatsApp",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
