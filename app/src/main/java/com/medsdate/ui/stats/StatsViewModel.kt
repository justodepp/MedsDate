package com.medsdate.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medsdate.data.repository.MedicineRepository
import com.medsdate.data.repository.SettingsRepository
import com.medsdate.domain.model.ExpiryStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MedicineStats(
    val total: Int = 0,
    val expired: Int = 0,
    val expiringSoon: Int = 0,
    val valid: Int = 0
)

class StatsViewModel(
    private val medicineRepository: MedicineRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val stats = combine(
        medicineRepository.getAllMedicines(),
        settingsRepository.getSettings().map { it.expiringSoonDays }
    ) { medicines, threshold ->
        MedicineStats(
            total = medicines.size,
            expired = medicines.count { it.isExpired() },
            expiringSoon = medicines.count { it.getExpiryStatus(threshold) == ExpiryStatus.EXPIRING_SOON },
            valid = medicines.count { it.getExpiryStatus(threshold) == ExpiryStatus.VALID }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MedicineStats()
    )
}
