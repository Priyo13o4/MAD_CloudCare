package com.example.cloudcareapp.ui.screens.consents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.model.Consent
import com.example.cloudcareapp.data.repository.MockDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConsentsViewModel(
    private val repository: MockDataRepository = MockDataRepository
) : ViewModel() {
    
    private val _pendingConsents = MutableStateFlow<List<Consent>>(emptyList())
    val pendingConsents: StateFlow<List<Consent>> = _pendingConsents.asStateFlow()
    
    private val _approvedConsents = MutableStateFlow<List<Consent>>(emptyList())
    val approvedConsents: StateFlow<List<Consent>> = _approvedConsents.asStateFlow()
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    init {
        loadConsents()
    }
    
    fun loadConsents() {
        viewModelScope.launch {
            _loading.value = true
            _pendingConsents.value = repository.getPendingConsents()
            _approvedConsents.value = repository.getApprovedConsents()
            _loading.value = false
        }
    }
}
