package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.dao.HadiyaDao
import com.example.data.model.HadiyaRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class HadiyaRepository(private val hadiyaDao: HadiyaDao) {

    val allRecords: Flow<List<HadiyaRecord>> = hadiyaDao.getAll()

    val totalAmount: Flow<Long> = hadiyaDao.getTotalAmount()

    val totalCount: Flow<Int> = hadiyaDao.getTotalCount()

    fun getTodayRecords(todayDate: String): Flow<List<HadiyaRecord>> {
        return hadiyaDao.getTodayRecords(todayDate)
    }

    fun getMonthRecords(monthPrefix: String): Flow<List<HadiyaRecord>> {
        return hadiyaDao.getMonthRecords(monthPrefix)
    }

    fun getYearRecords(yearPrefix: String): Flow<List<HadiyaRecord>> {
        return hadiyaDao.getYearRecords(yearPrefix)
    }

    fun search(query: String): Flow<List<HadiyaRecord>> {
        return hadiyaDao.searchRecords(query)
    }

    fun getByDateRange(startDate: String, endDate: String): Flow<List<HadiyaRecord>> {
        return hadiyaDao.getByDateRange(startDate, endDate)
    }

    suspend fun insert(record: HadiyaRecord): Long = withContext(Dispatchers.IO) {
        hadiyaDao.insert(record)
    }

    suspend fun update(record: HadiyaRecord) = withContext(Dispatchers.IO) {
        hadiyaDao.update(record.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(record: HadiyaRecord) = withContext(Dispatchers.IO) {
        hadiyaDao.delete(record)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        hadiyaDao.deleteById(id)
    }

    // --- CSV Export ---
    suspend fun generateCsvString(): String = withContext(Dispatchers.IO) {
        val records = hadiyaDao.getAllSync()
        val sb = StringBuilder()
        // Header
        sb.append("ID,Name,Phone,Amount,Date,Note,Created At,Updated At\n")

        for (record in records) {
            sb.append(record.id).append(",")
            sb.append(escapeCsv(record.name)).append(",")
            sb.append(escapeCsv(record.phone)).append(",")
            sb.append(record.amount).append(",")
            sb.append(escapeCsv(record.date)).append(",")
            sb.append(escapeCsv(record.note ?: "")).append(",")
            sb.append(record.createdAt).append(",")
            sb.append(record.updatedAt).append("\n")
        }
        sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val containsSpecial = value.contains(",") || value.contains("\"") || value.contains("\n")
        return if (containsSpecial) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    // --- JSON Backup ---
    suspend fun generateBackupJson(): String = withContext(Dispatchers.IO) {
        val records = hadiyaDao.getAllSync()
        val root = JSONObject()
        root.put("app", "HadiyaBook")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("recordCount", records.size)

        val array = JSONArray()
        for (r in records) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("name", r.name)
            obj.put("phone", r.phone)
            obj.put("amount", r.amount)
            obj.put("date", r.date)
            obj.put("note", r.note ?: "")
            obj.put("created_at", r.createdAt)
            obj.put("updated_at", r.updatedAt)
            array.put(obj)
        }
        root.put("records", array)
        root.toString(2)
    }

    // --- JSON Restore ---
    suspend fun restoreFromJson(jsonContent: String, overwrite: Boolean): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val root = JSONObject(jsonContent)
                if (!root.has("records")) {
                    return@withContext Result.failure(IllegalArgumentException("Invalid HadiyaBook backup file"))
                }

                val array = root.getJSONArray("records")
                val recordsToInsert = mutableListOf<HadiyaRecord>()

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val record = HadiyaRecord(
                        id = if (overwrite) obj.optLong("id", 0) else 0,
                        name = obj.getString("name"),
                        phone = obj.getString("phone"),
                        amount = obj.getLong("amount"),
                        date = obj.getString("date"),
                        note = obj.optString("note").ifBlank { null },
                        createdAt = obj.optLong("created_at", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updated_at", System.currentTimeMillis())
                    )
                    recordsToInsert.add(record)
                }

                if (overwrite) {
                    hadiyaDao.clearAll()
                }

                hadiyaDao.insertAll(recordsToInsert)
                Result.success(recordsToInsert.size)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // --- File IO via Storage Access Framework (SAF) ---
    suspend fun writeStringToUri(context: Context, uri: Uri, content: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                        writer.write(content)
                        writer.flush()
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun readStringFromUri(context: Context, uri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                        reader.readText()
                    }
                } ?: return@withContext Result.failure(IllegalStateException("Cannot open input stream"))
                Result.success(content)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
