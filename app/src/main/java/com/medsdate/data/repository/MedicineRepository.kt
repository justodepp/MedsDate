package com.medsdate.data.repository

import com.medsdate.data.local.dao.MedicineDao
import com.medsdate.data.local.entity.MedicineEntity
import com.medsdate.domain.model.Medicine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for medicine data operations.
 *
 * Acts as a single source of truth for medicine data, abstracting the data layer
 * from the UI layer and handling data transformations between entities and domain models.
 */
class MedicineRepository(private val medicineDao: MedicineDao) {

    /**
     * Retrieves all medicines as domain models.
     */
    fun getAllMedicines(): Flow<List<Medicine>> {
        return medicineDao.getAllMedicines().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Retrieves a specific medicine by ID.
     */
    fun getMedicineById(id: Int): Flow<Medicine?> {
        return medicineDao.getMedicineById(id).map { it?.toDomainModel() }
    }

    /**
     * Searches medicines by query string.
     */
    fun searchMedicines(query: String): Flow<List<Medicine>> {
        return medicineDao.searchMedicines(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Inserts a new medicine.
     */
    suspend fun insertMedicine(medicine: Medicine): Long {
        return medicineDao.insertMedicine(medicine.toEntity())
    }

    /**
     * Updates an existing medicine.
     */
    suspend fun updateMedicine(medicine: Medicine) {
        medicineDao.updateMedicine(medicine.toEntity())
    }

    /**
     * Deletes a medicine.
     */
    suspend fun deleteMedicine(medicine: Medicine) {
        medicineDao.deleteMedicine(medicine.toEntity())
    }

    /**
     * Deletes a medicine by ID.
     */
    suspend fun deleteMedicineById(id: Int) {
        medicineDao.deleteMedicineById(id)
    }
}

/**
 * Extension function to convert MedicineEntity to Medicine domain model.
 */
private fun MedicineEntity.toDomainModel(): Medicine {
    return Medicine(
        id = this.id,
        name = this.name,
        expiryDate = this.expiryDate,
        quantity = this.quantity,
        imagePath = this.imagePath,
        notes = this.notes,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

/**
 * Extension function to convert Medicine domain model to MedicineEntity.
 */
private fun Medicine.toEntity(): MedicineEntity {
    return MedicineEntity(
        id = this.id,
        name = this.name,
        expiryDate = this.expiryDate,
        quantity = this.quantity,
        imagePath = this.imagePath,
        notes = this.notes,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
