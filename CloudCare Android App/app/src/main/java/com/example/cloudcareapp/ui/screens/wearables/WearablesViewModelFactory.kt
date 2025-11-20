package com.example.cloudcareapp.ui.screens.wearables

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WearablesViewModelFactory(
    private val application: Application,
    private val patientId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WearablesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WearablesViewModel(application, patientId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
