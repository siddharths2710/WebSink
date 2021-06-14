package com.sid.websink.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DomainOverrideViewModel(application: Application): AndroidViewModel(application) {
    val getAll: LiveData<List<DomainOverrideMapping>>
    private val repository: DomainOverrideMappingRepository

    init {
        val domainOverrideMappingDao = SinkDatabase.getDatabase(application).domainOverrideMappingDao()
        repository = DomainOverrideMappingRepository(domainOverrideMappingDao)
        getAll = repository.getAll
    }

    fun addMapping(domainOverrideMapping: DomainOverrideMapping) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMapping(domainOverrideMapping)
        }
    }
}