package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.HadiyaDao
import com.example.data.model.HadiyaRecord

@Database(
    entities = [HadiyaRecord::class],
    version = 1,
    exportSchema = false
)
abstract class HadiyaDatabase : RoomDatabase() {

    abstract fun hadiyaDao(): HadiyaDao

    companion object {
        @Volatile
        private var INSTANCE: HadiyaDatabase? = null

        fun getDatabase(context: Context): HadiyaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HadiyaDatabase::class.java,
                    "hadiyabook_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
