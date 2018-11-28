package com.medsdate.data.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

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
