package com.medsdate.data.db

import android.app.Application
import androidx.lifecycle.LiveData
import com.medsdate.data.db.model.MedicineEntry

class MedsRepository private constructor(application: Application) {

    private val localDataSource: MedsLocalDataSource = MedsLocalDataSource(application)

    /**
     * Get the list of products from the database and get notified when the data changes.
     */
    suspend fun insertMedicine(medicineEntry: MedicineEntry) {
        localDataSource.insertMedicine(medicineEntry)
    }

    fun loadMedicines(): LiveData<List<MedicineEntry>> {
        return localDataSource.loadMedicines()
    }

    fun loadMedicine(medicineId: Int): LiveData<MedicineEntry> {
        return localDataSource.loadMedicine(medicineId)
    }

    suspend fun updateMedicine(medicineEntry: MedicineEntry) {
        localDataSource.updateMedicine(medicineEntry)
    }

    suspend fun deleteMedicine(medicineEntry: MedicineEntry) {
        localDataSource.deleteMedicine(medicineEntry)
    }

    companion object {
        @Volatile
        private var INSTANCE: MedsRepository? = null

        fun getInstance(
                application: Application
        ): MedsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE
                        ?: MedsRepository(
                                application
                        ).also { INSTANCE = it }
            }
        }
    }
}