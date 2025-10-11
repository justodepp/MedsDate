package com.medsdate.ui.add

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medsdate.data.repository.MedicineRepository
import com.medsdate.data.repository.SettingsRepository
import com.medsdate.domain.model.Medicine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date

/**
 * ViewModel for Add/Edit medicine screen.
 *
 * Handles form state, validation, and saving/updating medicines.
 */
class AddEditViewModel(
    private val context: Context,
    private val repository: MedicineRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    // Navigation events
    private val _events = MutableSharedFlow<AddEditEvent>()
    val events: SharedFlow<AddEditEvent> = _events.asSharedFlow()

    /**
     * Loads medicine data for editing.
     */
    fun loadMedicine(medicineId: Int) {
        viewModelScope.launch {
            repository.getMedicineById(medicineId)
                .firstOrNull()
                ?.let { medicine ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isEditMode = true,
                            medicineId = medicine.id,
                            name = medicine.name,
                            expiryDate = medicine.expiryDate,
                            imagePath = medicine.imagePath,
                            notes = medicine.notes ?: ""
                        )
                    }
                }
        }
    }

    /**
     * Updates medicine name.
     */
    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    /**
     * Updates expiry date.
     */
    fun onExpiryDateChange(date: Date) {
        _uiState.update { it.copy(expiryDate = date, expiryDateError = null) }
    }

    /**
     * Updates image path.
     */
    fun onImagePathChange(path: String?) {
        _uiState.update { it.copy(imagePath = path) }
    }

    /**
     * Updates notes.
     */
    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    /**
     * Validates and saves the medicine.
     */
    fun saveMedicine() {
        val currentState = _uiState.value

        // Validate
        val nameError = if (currentState.name.isBlank()) "Name is required" else null
        val expiryDateError = if (currentState.expiryDate.before(Date())) {
            "Expiry date must be in the future"
        } else null

        if (nameError != null || expiryDateError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    expiryDateError = expiryDateError
                )
            }
            return
        }

        // Save
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val medicine = Medicine(
                    id = currentState.medicineId,
                    name = currentState.name,
                    expiryDate = currentState.expiryDate,
                    imagePath = currentState.imagePath,
                    notes = currentState.notes.ifBlank { null },
                    updatedAt = Date()
                )

                if (currentState.isEditMode) {
                    repository.updateMedicine(medicine)
                    Timber.d("Medicine updated: ${medicine.name}")
                } else {
                    repository.insertMedicine(medicine)
                    Timber.d("Medicine inserted: ${medicine.name}")
                }

                // Schedule notifications
                scheduleNotifications(medicine)

                _events.emit(AddEditEvent.SaveSuccess)
            } catch (e: Exception) {
                Timber.e(e, "Error saving medicine")
                _events.emit(AddEditEvent.SaveError(e.message ?: "Unknown error"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    /**
     * Schedules notifications for the medicine.
     */
    private suspend fun scheduleNotifications(medicine: Medicine) {
        try {
            val settings = settingsRepository.getSettings().firstOrNull()
                ?: com.medsdate.domain.model.NotificationSettings()

            com.medsdate.worker.NotificationScheduler.scheduleMedicineNotifications(
                context,
                medicine,
                settings
            )
        } catch (e: Exception) {
            Timber.e(e, "Error scheduling notifications")
        }
    }
}

/**
 * UI state for Add/Edit screen.
 */
data class AddEditUiState(
    val isEditMode: Boolean = false,
    val medicineId: Int = 0,
    val name: String = "",
    val expiryDate: Date = Date(),
    val imagePath: String? = null,
    val notes: String = "",
    val nameError: String? = null,
    val expiryDateError: String? = null,
    val isSaving: Boolean = false
)

/**
 * Events for Add/Edit screen.
 */
sealed class AddEditEvent {
    object SaveSuccess : AddEditEvent()
    data class SaveError(val message: String) : AddEditEvent()
}
