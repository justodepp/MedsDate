package com.medsdate.ui.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medsdate.data.repository.MedicineRepository
import com.medsdate.domain.model.Medicine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for Detail screen.
 *
 * Displays medicine details and handles delete operations.
 */
class DetailViewModel(
    private val context: Context,
    private val repository: MedicineRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Navigation events
    private val _events = MutableSharedFlow<DetailEvent>()
    val events: SharedFlow<DetailEvent> = _events.asSharedFlow()

    /**
     * Loads medicine details.
     */
    fun loadMedicine(medicineId: Int) {
        viewModelScope.launch {
            repository.getMedicineById(medicineId)
                .catch { error ->
                    Timber.e(error, "Error loading medicine")
                    _uiState.value = DetailUiState.Error(error.message ?: "Unknown error")
                }
                .collect { medicine ->
                    if (medicine != null) {
                        _uiState.value = DetailUiState.Success(medicine)
                    } else {
                        _uiState.value = DetailUiState.Error("Medicine not found")
                    }
                }
        }
    }

    /**
     * Deletes the medicine.
     */
    fun deleteMedicine() {
        val currentState = _uiState.value
        if (currentState !is DetailUiState.Success) return

        viewModelScope.launch {
            try {
                val medicine = currentState.medicine

                // Cancel notifications first
                com.medsdate.worker.NotificationScheduler.cancelMedicineNotifications(
                    context,
                    medicine.id
                )

                // Then delete medicine
                repository.deleteMedicine(medicine)
                Timber.d("Medicine deleted: ${medicine.name}")
                _events.emit(DetailEvent.DeleteSuccess)
            } catch (e: Exception) {
                Timber.e(e, "Error deleting medicine")
                _events.emit(DetailEvent.DeleteError(e.message ?: "Unknown error"))
            }
        }
    }
}

/**
 * UI state for Detail screen.
 */
sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val medicine: Medicine) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

/**
 * Events for Detail screen.
 */
sealed class DetailEvent {
    object DeleteSuccess : DetailEvent()
    data class DeleteError(val message: String) : DetailEvent()
}
