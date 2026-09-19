package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.HadiyaRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HadiyaDao {

    @Query("SELECT * FROM hadiya_records ORDER BY date DESC, id DESC")
    fun getAll(): Flow<List<HadiyaRecord>>

    @Query("SELECT * FROM hadiya_records ORDER BY date DESC, id DESC")
    suspend fun getAllSync(): List<HadiyaRecord>

    @Query("SELECT * FROM hadiya_records WHERE id = :id")
    fun getById(id: Long): Flow<HadiyaRecord?>

    @Query("SELECT * FROM hadiya_records WHERE id = :id")
    suspend fun getByIdSync(id: Long): HadiyaRecord?

    @Query("SELECT * FROM hadiya_records WHERE date = :todayDate ORDER BY id DESC")
    fun getTodayRecords(todayDate: String): Flow<List<HadiyaRecord>>

    @Query("SELECT * FROM hadiya_records WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC, id DESC")
    fun getMonthRecords(monthPrefix: String): Flow<List<HadiyaRecord>>

    @Query("SELECT * FROM hadiya_records WHERE date LIKE :yearPrefix || '%' ORDER BY date DESC, id DESC")
    fun getYearRecords(yearPrefix: String): Flow<List<HadiyaRecord>>

    @Query("SELECT * FROM hadiya_records WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY date DESC, id DESC")
    fun searchRecords(query: String): Flow<List<HadiyaRecord>>

    @Query("SELECT * FROM hadiya_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, id DESC")
    fun getByDateRange(startDate: String, endDate: String): Flow<List<HadiyaRecord>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM hadiya_records")
    fun getTotalAmount(): Flow<Long>

    @Query("SELECT COUNT(*) FROM hadiya_records")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HadiyaRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<HadiyaRecord>): List<Long>

    @Update
    suspend fun update(record: HadiyaRecord)

    @Delete
    suspend fun delete(record: HadiyaRecord)

    @Query("DELETE FROM hadiya_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM hadiya_records")
    suspend fun clearAll()
}
