package com.sid.websink.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PinnerViewModel(application: Application): AndroidViewModel(application) {
    val getAll: LiveData<List<PinnerMapping>>
    private val repository: PinnerMappingRepository

    init {
        val pinnerMappingDao = SinkDatabase.getDatabase(application).pinnerMappingDao()
        repository = PinnerMappingRepository(pinnerMappingDao)
        getAll = repository.getAll
    }

    fun addMapping(pinnerMapping: PinnerMapping) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMapping(pinnerMapping)
        }
    }
}