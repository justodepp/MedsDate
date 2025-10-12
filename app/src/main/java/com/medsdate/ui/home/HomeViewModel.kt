package com.medsdate.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medsdate.data.repository.MedicineRepository
import com.medsdate.domain.model.Medicine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the Home screen.
 *
 * Manages the list of medicines, search functionality, and grouping by expiry status.
 */
class HomeViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // All medicines from repository
    private val allMedicines: Flow<List<Medicine>> = repository.getAllMedicines()

    // Filtered medicines based on search
    @OptIn(ExperimentalCoroutinesApi::class)
    val medicines: StateFlow<List<Medicine>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                allMedicines
            } else {
                repository.searchMedicines(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Grouped medicines (expired and active)
    val groupedMedicines: StateFlow<GroupedMedicines> = medicines
        .map { list ->
            val expired = list.filter { it.isExpired() }
            val active = list.filter { !it.isExpired() }
            GroupedMedicines(expired = expired, active = active)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GroupedMedicines(emptyList(), emptyList())
        )

    /**
     * Updates the search query.
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        Timber.d("Search query updated: $query")
    }

    /**
     * Clears the search query.
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }

    /**
     * Deletes a medicine.
     */
    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            try {
                repository.deleteMedicine(medicine)
                Timber.d("Medicine deleted: ${medicine.name}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete medicine: ${medicine.name}")
            }
        }
    }
}

/**
 * Data class for grouped medicines.
 */
data class GroupedMedicines(
    val expired: List<Medicine>,
    val active: List<Medicine>
)
