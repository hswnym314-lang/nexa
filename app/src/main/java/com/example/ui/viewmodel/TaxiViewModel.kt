package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Driver
import com.example.data.model.RideStatus
import com.example.data.repository.TaxiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaxiViewModel(
    private val repository: TaxiRepository = TaxiRepository()
) : ViewModel() {

    val drivers: StateFlow<List<Driver>> = repository.drivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rideStatus: StateFlow<RideStatus> = repository.rideStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RideStatus.IDLE)

    val assignedDriver: StateFlow<Driver?> = repository.assignedDriver
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isServerOnline: StateFlow<Boolean> = repository.isServerOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val serverMessage: StateFlow<String?> = repository.serverMessage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "جاري الاتصال بسيرفر Nexa Taxi...")

    val distanceMeters: StateFlow<Int> = repository.distanceToUserMeters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val etaSeconds: StateFlow<Int> = repository.etaSeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedDestination = MutableStateFlow("كورنيش شط العرب")
    val selectedDestination: StateFlow<String> = _selectedDestination.asStateFlow()

    fun selectDestination(destination: String) {
        _selectedDestination.value = destination
    }

    fun requestTaxi() {
        viewModelScope.launch {
            repository.requestRide(_selectedDestination.value)
        }
    }

    fun cancelRide() {
        repository.cancelRide()
    }

    fun completeRide() {
        repository.completeRide()
    }

    fun retryServer() {
        viewModelScope.launch {
            repository.fetchDriversFromServer()
        }
    }
}
