package com.sid.websink.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PinnerMappingDao {
    @Query("SELECT * FROM pinner_mapping")
    fun getAll(): LiveData<List<PinnerMapping>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addMapping(mapping: PinnerMapping)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(vararg mappings: PinnerMapping)

    @Delete
    fun deleteMapping(mapping: PinnerMapping)
}