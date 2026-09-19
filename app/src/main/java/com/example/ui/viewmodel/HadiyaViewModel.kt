package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.HadiyaDatabase
import com.example.data.model.HadiyaRecord
import com.example.data.repository.HadiyaRepository
import com.example.security.PinManager
import com.example.util.DateUtils
import com.example.util.WhatsAppHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class HistoryFilter {
    ALL, TODAY, THIS_WEEK, THIS_MONTH, CUSTOM_RANGE
}

enum class HistorySort {
    NEWEST_FIRST, OLDEST_FIRST, HIGHEST_AMOUNT, LOWEST_AMOUNT
}

enum class ReportPeriod {
    DAILY, MONTHLY, YEARLY
}

data class DashboardStats(
    val todayCount: Int = 0,
    val todayTotal: Long = 0L,
    val monthCount: Int = 0,
    val monthTotal: Long = 0L,
    val totalCount: Int = 0,
    val totalAmount: Long = 0L,
    val recentRecords: List<HadiyaRecord> = emptyList()
)

data class AddHadiyaFormState(
    val name: String = "",
    val phone: String = "",
    val amount: String = "",
    val date: String = DateUtils.getTodayIso(),
    val note: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
    val amountError: String? = null
)

data class HistoryFilterParams(
    val query: String = "",
    val filter: HistoryFilter = HistoryFilter.ALL,
    val sort: HistorySort = HistorySort.NEWEST_FIRST,
    val customStartDate: String = DateUtils.getTodayIso(),
    val customEndDate: String = DateUtils.getTodayIso()
)

class HadiyaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HadiyaRepository
    val pinManager: PinManager = PinManager(application)
    private val prefs = application.getSharedPreferences("hadiyabook_app_prefs", Context.MODE_PRIVATE)

    init {
        val database = HadiyaDatabase.getDatabase(application)
        repository = HadiyaRepository(database.hadiyaDao())
    }

    // --- Theme State ---
    private val _themeMode = MutableStateFlow(
        when (prefs.getString("theme_mode", "LIGHT")) {
            "DARK" -> ThemeMode.DARK
            "SYSTEM" -> ThemeMode.SYSTEM
            else -> ThemeMode.LIGHT
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    // --- PIN / Security State ---
    private val _isLocked = MutableStateFlow(pinManager.isPinEnabled())
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun unlockApp(pin: String): Boolean {
        val isValid = pinManager.verifyPin(pin)
        if (isValid) {
            _isLocked.value = false
        }
        return isValid
    }

    fun lockApp() {
        if (pinManager.isPinEnabled()) {
            _isLocked.value = true
        }
    }

    fun setPin(pin: String) {
        pinManager.setPin(pin)
        _isLocked.value = false
    }

    fun removePin() {
        pinManager.removePin()
        _isLocked.value = false
    }

    // --- All Records Flow ---
    val allRecords: StateFlow<List<HadiyaRecord>> = repository.allRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Dashboard Stats ---
    val dashboardStats: StateFlow<DashboardStats> = allRecords.combine(
        MutableStateFlow(DateUtils.getTodayIso())
    ) { records, todayIso ->
        val monthPrefix = DateUtils.getCurrentMonthPrefix()

        val todayList = records.filter { it.date == todayIso }
        val monthList = records.filter { it.date.startsWith(monthPrefix) }

        DashboardStats(
            todayCount = todayList.size,
            todayTotal = todayList.sumOf { it.amount },
            monthCount = monthList.size,
            monthTotal = monthList.sumOf { it.amount },
            totalCount = records.size,
            totalAmount = records.sumOf { it.amount },
            recentRecords = records.take(5)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats()
    )

    // --- Add Form State ---
    private val _addFormState = MutableStateFlow(AddHadiyaFormState())
    val addFormState: StateFlow<AddHadiyaFormState> = _addFormState.asStateFlow()

    fun updateAddName(name: String) {
        _addFormState.value = _addFormState.value.copy(name = name, nameError = null)
    }

    fun updateAddPhone(phone: String) {
        _addFormState.value = _addFormState.value.copy(phone = phone, phoneError = null)
    }

    fun updateAddAmount(amount: String) {
        // Keep digits only
        val filtered = amount.filter { it.isDigit() }
        _addFormState.value = _addFormState.value.copy(amount = filtered, amountError = null)
    }

    fun updateAddDate(dateIso: String) {
        _addFormState.value = _addFormState.value.copy(date = dateIso)
    }

    fun updateAddNote(note: String) {
        _addFormState.value = _addFormState.value.copy(note = note)
    }

    fun resetAddForm() {
        _addFormState.value = AddHadiyaFormState(date = DateUtils.getTodayIso())
    }

    // --- Snackbar / Events ---
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    fun showMessage(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }

    fun saveHadiya(onSaved: (HadiyaRecord) -> Unit): Boolean {
        val form = _addFormState.value
        var hasError = false
        var nameErr: String? = null
        var phoneErr: String? = null
        var amountErr: String? = null

        if (form.name.trim().isBlank()) {
            nameErr = "Person name is required"
            hasError = true
        }

        if (form.phone.trim().isBlank()) {
            phoneErr = "Phone number is required"
            hasError = true
        } else if (!WhatsAppHelper.isValidIndianPhoneNumber(form.phone)) {
            phoneErr = "Enter a valid 10-digit Indian mobile number"
            hasError = true
        }

        val amountLong = form.amount.toLongOrNull()
        if (amountLong == null || amountLong <= 0) {
            amountErr = "Enter a valid amount greater than 0"
            hasError = true
        }

        if (hasError) {
            _addFormState.value = form.copy(
                nameError = nameErr,
                phoneError = phoneErr,
                amountError = amountErr
            )
            return false
        }

        val record = HadiyaRecord(
            name = form.name.trim(),
            phone = form.phone.trim(),
            amount = amountLong!!,
            date = form.date,
            note = form.note.trim().ifBlank { null }
        )

        viewModelScope.launch {
            val generatedId = repository.insert(record)
            val savedRecord = record.copy(id = generatedId)
            resetAddForm()
            showMessage("Hadiya saved successfully.")
            onSaved(savedRecord)
        }
        return true
    }

    // --- Edit Record ---
    fun updateRecord(
        record: HadiyaRecord,
        newName: String,
        newPhone: String,
        newAmount: Long,
        newDate: String,
        newNote: String?,
        onSuccess: () -> Unit
    ) {
        if (newName.isBlank()) {
            showMessage("Name cannot be empty")
            return
        }
        if (!WhatsAppHelper.isValidIndianPhoneNumber(newPhone)) {
            showMessage("Invalid Indian mobile number")
            return
        }
        if (newAmount <= 0) {
            showMessage("Amount must be greater than 0")
            return
        }

        viewModelScope.launch {
            val updated = record.copy(
                name = newName.trim(),
                phone = newPhone.trim(),
                amount = newAmount,
                date = newDate,
                note = newNote?.trim()?.ifBlank { null }
            )
            repository.update(updated)
            showMessage("Record updated successfully.")
            onSuccess()
        }
    }

    // --- Delete Record ---
    fun deleteRecord(record: HadiyaRecord, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.delete(record)
            showMessage("Hadiya record deleted.")
            onDeleted()
        }
    }

    // --- History Screen State & Filter ---
    val searchQuery = MutableStateFlow("")
    val historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val historySort = MutableStateFlow(HistorySort.NEWEST_FIRST)
    val customStartDate = MutableStateFlow(DateUtils.getTodayIso())
    val customEndDate = MutableStateFlow(DateUtils.getTodayIso())

    private val filterParams = combine(
        combine(searchQuery, historyFilter) { q, f -> Pair(q, f) },
        combine(historySort, customStartDate, customEndDate) { s, startD, endD -> Triple(s, startD, endD) }
    ) { (q, f), (s, startD, endD) ->
        HistoryFilterParams(q, f, s, startD, endD)
    }

    val filteredRecords: StateFlow<List<HadiyaRecord>> = combine(
        allRecords,
        filterParams
    ) { records, params ->
        val todayIso = DateUtils.getTodayIso()
        val startOfWeek = DateUtils.getStartOfWeekIso()
        val endOfWeek = DateUtils.getEndOfWeekIso()
        val monthPrefix = DateUtils.getCurrentMonthPrefix()

        // 1. Filter by query
        var list = if (params.query.isBlank()) {
            records
        } else {
            val q = params.query.trim().lowercase()
            records.filter {
                it.name.lowercase().contains(q) || it.phone.contains(q)
            }
        }

        // 2. Filter by date category
        list = when (params.filter) {
            HistoryFilter.ALL -> list
            HistoryFilter.TODAY -> list.filter { it.date == todayIso }
            HistoryFilter.THIS_WEEK -> list.filter { it.date in startOfWeek..endOfWeek }
            HistoryFilter.THIS_MONTH -> list.filter { it.date.startsWith(monthPrefix) }
            HistoryFilter.CUSTOM_RANGE -> {
                val s = if (params.customStartDate <= params.customEndDate) params.customStartDate else params.customEndDate
                val e = if (params.customStartDate <= params.customEndDate) params.customEndDate else params.customStartDate
                list.filter { it.date in s..e }
            }
        }

        // 3. Sort
        when (params.sort) {
            HistorySort.NEWEST_FIRST -> list.sortedWith(compareByDescending<HadiyaRecord> { it.date }.thenByDescending { it.id })
            HistorySort.OLDEST_FIRST -> list.sortedWith(compareBy<HadiyaRecord> { it.date }.thenBy { it.id })
            HistorySort.HIGHEST_AMOUNT -> list.sortedByDescending { it.amount }
            HistorySort.LOWEST_AMOUNT -> list.sortedBy { it.amount }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Reports Screen State ---
    val reportPeriod = MutableStateFlow(ReportPeriod.MONTHLY)
    val selectedReportDate = MutableStateFlow(DateUtils.getTodayIso())
    val selectedReportYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedReportMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)

    // --- Backup & Restore & CSV Execution ---
    fun exportBackup(context: Context, uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = repository.generateBackupJson()
                val res = repository.writeStringToUri(context, uri, json)
                if (res.isSuccess) {
                    onResult(true, "Backup saved successfully.")
                } else {
                    onResult(false, res.exceptionOrNull()?.localizedMessage ?: "Failed to write backup")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Error during backup")
            }
        }
    }

    fun restoreBackup(context: Context, uri: Uri, overwrite: Boolean, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val readRes = repository.readStringFromUri(context, uri)
                if (readRes.isFailure) {
                    onResult(false, "Could not read backup file")
                    return@launch
                }
                val jsonContent = readRes.getOrNull().orEmpty()
                val restoreRes = repository.restoreFromJson(jsonContent, overwrite)
                if (restoreRes.isSuccess) {
                    val count = restoreRes.getOrNull() ?: 0
                    onResult(true, "Successfully restored $count Hadiya records.")
                } else {
                    onResult(false, restoreRes.exceptionOrNull()?.localizedMessage ?: "Invalid backup file")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Error during restore")
            }
        }
    }

    fun exportCsv(context: Context, uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val csvString = repository.generateCsvString()
                val res = repository.writeStringToUri(context, uri, csvString)
                if (res.isSuccess) {
                    onResult(true, "CSV exported successfully.")
                } else {
                    onResult(false, res.exceptionOrNull()?.localizedMessage ?: "Failed to write CSV")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Error during CSV export")
            }
        }
    }
}
