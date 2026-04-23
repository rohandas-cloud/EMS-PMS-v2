package com.example.myapplication.viewmodel

import androidx.lifecycle.*
import com.example.myapplication.data.model.Holiday
import com.example.myapplication.data.repository.HolidayRepository
import kotlinx.coroutines.launch

class HolidayViewModel : ViewModel() {

    private val repository = HolidayRepository()

    private val _holidays = MutableLiveData<List<Holiday>>()
    val holidays: LiveData<List<Holiday>> get() = _holidays

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // =========================================================
    // LOAD HOLIDAYS
    // =========================================================
    fun loadMyHolidays() {

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {

            try {
                val result = repository.getMyHoliday()
                result.onSuccess { list ->
                    _holidays.value = list
                    if (list.isEmpty()) {
                        _error.value = "No holidays found"
                    }
                }.onFailure { error ->
                    _holidays.value = emptyList()
                    _error.value = error.message ?: "Failed to load holidays"
                }

            } catch (e: Exception) {
                _holidays.value = emptyList()
                _error.value = "Failed to load holidays"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllLocationHolidays() = loadMyHolidays()

    fun loadMyLocationHolidays() = loadMyHolidays()
}