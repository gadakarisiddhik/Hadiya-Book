package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hadiya_records",
    indices = [
        Index(value = ["name"]),
        Index(value = ["phone"]),
        Index(value = ["date"])
    ]
)
data class HadiyaRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val amount: Long, // Amount in Indian Rupees (₹)
    val date: String, // Stored as ISO format: YYYY-MM-DD
    val note: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
