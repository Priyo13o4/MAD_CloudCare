package com.example.cloudcareapp.ui.screens.consents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.ConsentResponse
import com.example.cloudcareapp.data.model.UpdateConsentRequest
import com.example.cloudcareapp.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConsentsViewModel : ViewModel() {
    
    private val _pendingConsents = MutableStateFlow<List<ConsentResponse>>(emptyList())
    val pendingConsents: StateFlow<List<ConsentResponse>> = _pendingConsents.asStateFlow()
    
    private val _approvedConsents = MutableStateFlow<List<ConsentResponse>>(emptyList())
    val approvedConsents: StateFlow<List<ConsentResponse>> = _approvedConsents.asStateFlow()
    
    private val _deniedConsents = MutableStateFlow<List<ConsentResponse>>(emptyList())
    val deniedConsents: StateFlow<List<ConsentResponse>> = _deniedConsents.asStateFlow()
    
    private val _revokedConsents = MutableStateFlow<List<ConsentResponse>>(emptyList())
    val revokedConsents: StateFlow<List<ConsentResponse>> = _revokedConsents.asStateFlow()
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    private val _updatingConsentId = MutableStateFlow<String?>(null)
    val updatingConsentId: StateFlow<String?> = _updatingConsentId.asStateFlow()
    
    init {
        loadConsents()
    }
    
    fun loadConsents() {
        viewModelScope.launch {
            _loading.value = true
            try {
                // ✅ FIX: Use real API instead of mock data
                val patientId = AppDataCache.getPatientId()
                if (patientId != null) {
                    // Load pending consents
                    val pending = RetrofitClient.apiService.getPatientConsents(
                        patientId = patientId,
                        statusFilter = "PENDING"
                    )
                    _pendingConsents.value = pending
                    
                    // Load approved consents
                    val approved = RetrofitClient.apiService.getPatientConsents(
                        patientId = patientId,
                        statusFilter = "APPROVED"
                    )
                    _approvedConsents.value = approved
                    
                    // Load denied consents
                    val denied = RetrofitClient.apiService.getPatientConsents(
                        patientId = patientId,
                        statusFilter = "DENIED"
                    )
                    _deniedConsents.value = denied
                    
                    // Load revoked consents
                    val revoked = RetrofitClient.apiService.getPatientConsents(
                        patientId = patientId,
                        statusFilter = "REVOKED"
                    )
                    _revokedConsents.value = revoked
                }
            } catch (e: Exception) {
                // Keep empty lists on error
                _pendingConsents.value = emptyList()
                _approvedConsents.value = emptyList()
                _deniedConsents.value = emptyList()
                _revokedConsents.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun updateConsentStatus(consentId: String, status: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _updatingConsentId.value = consentId
            try {
                val request = UpdateConsentRequest(status = status)
                RetrofitClient.apiService.updateConsentStatus(consentId, request)
                
                // Reload consents to refresh UI
                loadConsents()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update consent")
            } finally {
                _updatingConsentId.value = null
            }
        }
    }
}
