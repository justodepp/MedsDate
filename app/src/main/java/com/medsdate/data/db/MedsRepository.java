package com.medsdate.data.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import com.medsdate.data.db.model.MedicineEntry;
import com.medsdate.utils.AppExecutors;

import java.util.List;

public class MedsRepository {

    private static MedsRepository sInstance;

    private final AppDatabase mDatabase;
    private MediatorLiveData<List<MedicineEntry>> mObservableMeds;

    private MedsRepository(final AppDatabase database) {
        mDatabase = database;
        mObservableMeds = new MediatorLiveData<>();

        mObservableMeds.addSource(mDatabase.medicineDao().loadAllMeds(),
                productEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableMeds.postValue(productEntities);
                    }
                });
    }

    public static MedsRepository getInstance(final AppDatabase database) {
        if (sInstance == null) {
            synchronized (MedsRepository.class) {
                if (sInstance == null) {
                    sInstance = new MedsRepository(database);
                }
            }
        }
        return sInstance;
    }

    /**
     * Get the list of products from the database and get notified when the data changes.
     */
    public void insertMedicine(final MedicineEntry medicineEntry) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.medicineDao().insertMedicine(medicineEntry);
            }
        });
    }

    public LiveData<List<MedicineEntry>> getMeds() {
        return mObservableMeds;
    }

    public LiveData<MedicineEntry> loadMedicine(final int medicineId) {
        return mDatabase.medicineDao().loadMedicineById(medicineId);
    }

    public void updateMedicine(final MedicineEntry medicineEntry) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.medicineDao().updateMedicine(medicineEntry);
            }
        });
    }

    public void deleteMedicine(final MedicineEntry medicineEntry) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.medicineDao().deleteMedicine(medicineEntry);
            }
        });
    }
}
