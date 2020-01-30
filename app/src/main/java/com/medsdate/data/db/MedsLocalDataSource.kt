/*
 * Copyright (c) 2018-present. Gianni Andrea Cavalli
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership. The ASF licenses this
 * file to you under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.medsdate.data.db

import android.content.Context
import androidx.lifecycle.LiveData
import com.medsdate.data.db.model.MedicineEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Local storage for MedsLocalDataSource related data, implemented using Room
 */
class MedsLocalDataSource(context: Context) {

    private val appDatabase: AppDatabase = AppDatabase.getInstance(context)

    /**
     * Get the list of products from the database and get notified when the data changes.
     */
    suspend fun insertMedicine(medicineEntry: MedicineEntry) {
        withContext(Dispatchers.IO) {
            appDatabase.medicineDao().insertMedicine(medicineEntry)
        }
    }

    fun loadMedicines(): LiveData<List<MedicineEntry>> {
        return appDatabase.medicineDao().loadAllMeds()
    }

    fun loadMedicine(medicineId: Int): LiveData<MedicineEntry> {
        return appDatabase.medicineDao().loadMedicineById(medicineId)
    }

    suspend fun updateMedicine(medicineEntry: MedicineEntry) {
        withContext(Dispatchers.IO) {
            appDatabase.medicineDao().updateMedicine(medicineEntry)
        }
    }

    suspend fun deleteMedicine(medicineEntry: MedicineEntry) {
        withContext(Dispatchers.IO) {
            appDatabase.medicineDao().updateMedicine(medicineEntry)
        }
    }
}