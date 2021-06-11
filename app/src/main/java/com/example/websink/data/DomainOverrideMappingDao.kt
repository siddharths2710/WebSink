package com.example.websink.data

import androidx.room.*

@Dao
interface DomainOverrideMappingDao {
    @Query("Select * from domain_override_mapping")
    fun getAll(): List<DomainOverrideMapping>

    @Query("Select new_domain from domain_override_mapping where old_domain IN (:oldDomain)")
    fun fetchDomainOverride(oldDomain: String): String

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addMapping(mapping: DomainOverrideMapping)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(vararg mappings: DomainOverrideMapping)

    @Delete
    fun deleteMapping(mapping: DomainOverrideMapping)
}