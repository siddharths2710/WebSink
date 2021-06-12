package com.sid.websink.data

import androidx.lifecycle.LiveData

class PinnerMappingRepository(private val pinnerMappingDao: PinnerMappingDao) {
    val getAll: LiveData<List<PinnerMapping>> = pinnerMappingDao.getAll()

    suspend fun addMapping(pinnerMapping: PinnerMapping) {
        pinnerMappingDao.addMapping(pinnerMapping)
    }
}