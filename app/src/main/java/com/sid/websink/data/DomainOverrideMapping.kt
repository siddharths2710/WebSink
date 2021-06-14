package com.sid.websink.data

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "domain_override_mapping")
data class DomainOverrideMapping (
    @PrimaryKey @NonNull @ColumnInfo(name="old_domain") val oldDomain: String,
    @ColumnInfo(name="new_domain") val newDomain: String?
)