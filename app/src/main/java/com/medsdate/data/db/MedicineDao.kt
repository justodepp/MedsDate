package com.medsdate.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.medsdate.data.db.model.MedicineEntry

@Dao
interface MedicineDao {

    @Query("SELECT * FROM medicine ORDER BY expire_at ASC")
    fun loadAllMeds(): LiveData<List<MedicineEntry>>

    @Insert
    fun insertMedicine(medicineEntry: MedicineEntry)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateMedicine(medicineEntry: MedicineEntry)

    @Delete
    fun deleteMedicine(medicineEntry: MedicineEntry)

    @Query("SELECT * FROM medicine WHERE id = :id")
    fun loadMedicineById(id: Int): LiveData<MedicineEntry>
}
