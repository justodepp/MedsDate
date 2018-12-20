package com.medsdate.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.medsdate.data.db.model.MedicineEntry;

import java.util.List;

@Dao
public interface MedicineDao {

    @Query("SELECT * FROM medicine ORDER BY expire_at ASC")
    LiveData<List<MedicineEntry>> loadAllMeds();

    @Insert
    void insertMedicine(MedicineEntry medicineEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMedicine(MedicineEntry medicineEntry);

    @Delete
    void deleteMedicine(MedicineEntry medicineEntry);

    @Query("SELECT * FROM medicine WHERE id = :id")
    LiveData<MedicineEntry> loadMedicineById(int id);
}
