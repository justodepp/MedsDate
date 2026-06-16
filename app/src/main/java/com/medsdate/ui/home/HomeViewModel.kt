package com.medsdate.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medsdate.data.repository.MedicineRepository
import com.medsdate.data.repository.SettingsRepository
import com.medsdate.domain.model.Medicine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the Home screen.
 *
 * Manages the list of medicines, search functionality, and grouping by expiry status.
 */
class HomeViewModel(
    private val repository: MedicineRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _pendingDelete = MutableStateFlow<Medicine?>(null)
    private var pendingDeleteJob: Job? = null

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.EXPIRY_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val expiringSoonDays: StateFlow<Int> = settingsRepository.getSettings()
        .map { it.expiringSoonDays }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 7
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredMedicines: Flow<List<Medicine>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllMedicines()
            else repository.searchMedicines(query)
        }

    val medicines: StateFlow<List<Medicine>> = combine(
        filteredMedicines,
        _pendingDelete,
        _sortOrder
    ) { list, pending, sort ->
        val filtered = if (pending != null) list.filter { it.id != pending.id } else list
        when (sort) {
            SortOrder.EXPIRY_ASC -> filtered.sortedBy { it.expiryDate }
            SortOrder.EXPIRY_DESC -> filtered.sortedByDescending { it.expiryDate }
            SortOrder.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
        }
    }.stateIn(
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

    fun deleteMedicine(medicine: Medicine) {
        pendingDeleteJob?.cancel()
        _pendingDelete.value = medicine
        pendingDeleteJob = viewModelScope.launch {
            _events.send(HomeEvent.ShowUndoSnackbar(medicine.name))
            delay(5_000)
            try {
                repository.deleteMedicine(medicine)
                Timber.d("Medicine deleted: ${medicine.name}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete medicine: ${medicine.name}")
            } finally {
                _pendingDelete.value = null
            }
        }
    }

    fun undoDelete() {
        pendingDeleteJob?.cancel()
        _pendingDelete.value = null
        Timber.d("Delete undone")
    }

    fun onSortOrderChange(order: SortOrder) {
        _sortOrder.value = order
    }
}

data class GroupedMedicines(
    val expired: List<Medicine>,
    val active: List<Medicine>
)

sealed class HomeEvent {
    data class ShowUndoSnackbar(val medicineName: String) : HomeEvent()
}

enum class SortOrder { EXPIRY_ASC, EXPIRY_DESC, NAME_ASC, NAME_DESC }
