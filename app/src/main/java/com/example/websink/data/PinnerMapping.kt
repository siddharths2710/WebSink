package com.example.websink.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinner_mapping")
data class PinnerMapping (
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name="domain") val domain: String?,
    @ColumnInfo(name="hash_type") val hashType: String?,
    @ColumnInfo(name="hash_val") val hashVal: String?
)