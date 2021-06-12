package com.sid.websink.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DomainOverrideMappingDao {

    @Query("SELECT new_domain FROM domain_override_mapping WHERE old_domain IN (:oldDomain)")
    fun fetchDomainOverride(oldDomain: String): String

    @Query("SELECT * FROM domain_override_mapping")
    fun getAll(): LiveData<List<DomainOverrideMapping>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addMapping(mapping: DomainOverrideMapping)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(vararg mappings: DomainOverrideMapping)

    @Delete
    fun deleteMapping(mapping: DomainOverrideMapping)
}