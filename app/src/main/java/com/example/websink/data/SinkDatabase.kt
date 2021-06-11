package com.example.websink.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DomainOverrideMapping::class, PinnerMapping::class], version = 1, exportSchema = false)
abstract class SinkDatabase : RoomDatabase() {
    abstract fun domainOverrideMappingDao(): DomainOverrideMappingDao
    abstract fun pinnerMappingDao(): PinnerMappingDao

    companion object{
        @Volatile
        private var INSTANCE: SinkDatabase? = null

        fun getDatabase(context: Context): SinkDatabase {
            val tempInstance = INSTANCE
            if(tempInstance != null)
                return tempInstance
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SinkDatabase::class.java,
                    "sink_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}