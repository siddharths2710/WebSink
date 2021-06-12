package com.sid.websink.data


import androidx.lifecycle.LiveData

class DomainOverrideMappingRepository(private val domainOverrideMappingDao: DomainOverrideMappingDao) {
    val getAll: LiveData<List<DomainOverrideMapping>> = domainOverrideMappingDao.getAll()

    suspend fun addMapping(domainOverrideMapping: DomainOverrideMapping) {
        domainOverrideMappingDao.addMapping(domainOverrideMapping)
    }
}