package com.example.websink.data

import androidx.room.*

@Dao
interface PinnerMappingDao {
    @Query("Select * from pinner_mapping")
    fun getAll(): List<PinnerMapping>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addMapping(mapping: PinnerMapping)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(vararg mappings: PinnerMapping)

    @Delete
    fun deleteMapping(mapping: PinnerMapping)
}