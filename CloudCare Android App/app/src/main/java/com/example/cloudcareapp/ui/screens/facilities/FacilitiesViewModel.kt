package com.example.cloudcareapp.ui.screens.facilities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.model.Facility
import com.example.cloudcareapp.data.repository.MockDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FacilitiesViewModel(
    private val repository: MockDataRepository = MockDataRepository
) : ViewModel() {
    
    private val _facilities = MutableStateFlow<List<Facility>>(emptyList())
    val facilities: StateFlow<List<Facility>> = _facilities.asStateFlow()
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    init {
        loadFacilities()
    }
    
    fun loadFacilities() {
        viewModelScope.launch {
            _loading.value = true
            _facilities.value = repository.getLinkedFacilities()
            _loading.value = false
        }
    }
}
