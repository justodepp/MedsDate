package com.medsdate.data.local.dao

import androidx.room.*
import com.medsdate.data.local.entity.MedicineEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for medicine operations.
 *
 * Provides methods to interact with the medicine table in the Room database.
 * All query results are returned as Flow for reactive updates.
 */
@Dao
interface MedicineDao {

    /**
     * Retrieves all medicines sorted by expiry date (earliest first).
     *
     * @return Flow of list of all medicines
     */
    @Query("SELECT * FROM medicine ORDER BY expiry_date ASC")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    /**
     * Retrieves a specific medicine by its ID.
     *
     * @param id The medicine ID
     * @return Flow of the medicine entity, or null if not found
     */
    @Query("SELECT * FROM medicine WHERE id = :id")
    fun getMedicineById(id: Int): Flow<MedicineEntity?>

    /**
     * Searches medicines by name or notes.
     *
     * @param searchQuery The search term
     * @return Flow of list of matching medicines
     */
    @Query("SELECT * FROM medicine WHERE name LIKE '%' || :searchQuery || '%' OR notes LIKE '%' || :searchQuery || '%' ORDER BY expiry_date ASC")
    fun searchMedicines(searchQuery: String): Flow<List<MedicineEntity>>

    /**
     * Inserts a new medicine into the database.
     *
     * @param medicine The medicine entity to insert
     * @return The row ID of the newly inserted medicine
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity): Long

    /**
     * Updates an existing medicine.
     *
     * @param medicine The medicine entity with updated values
     */
    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    /**
     * Deletes a medicine from the database.
     *
     * @param medicine The medicine entity to delete
     */
    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    /**
     * Deletes a medicine by its ID.
     *
     * @param id The medicine ID
     */
    @Query("DELETE FROM medicine WHERE id = :id")
    suspend fun deleteMedicineById(id: Int)

    /**
     * Deletes all medicines from the database.
     */
    @Query("DELETE FROM medicine")
    suspend fun deleteAllMedicines()
}
