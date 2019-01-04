/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.medsdate.ui.viewmodel;

import android.app.Application;

import com.medsdate.data.db.AppDatabase;
import com.medsdate.data.db.MedsRepository;
import com.medsdate.data.db.model.MedicineEntry;
import com.medsdate.utils.AppExecutors;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MedsViewModel extends AndroidViewModel {

    private MedsRepository medsRepository;

    private LiveData<List<MedicineEntry>> meds;

    public MedsViewModel(Application application) {
        super(application);
        medsRepository = MedsRepository.getInstance(AppDatabase.getInstance(application, AppExecutors.getInstance()));
        //Timber.d( "Actively retrieving the tasks from the DataBase");
        meds = medsRepository.getMeds();
    }

    public LiveData<List<MedicineEntry>> getMeds() {
        return meds;
    }

    public void insert(MedicineEntry medicine) {
        medsRepository.insertMedicine(medicine);
    }

    public void delete(MedicineEntry medicine) {
        medsRepository.deleteMedicine(medicine);
    }

    public LiveData<MedicineEntry> load(int medicineId) {

        return medsRepository.loadMedicine(medicineId);
    }

    public void update(MedicineEntry medicine) {
        medsRepository.updateMedicine(medicine);
    }
}
