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
package com.medsdate.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.medsdate.data.db.MedsRepository
import com.medsdate.data.db.model.MedicineEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedsViewModel(application: Application) : AndroidViewModel(application) {

    private var _meds= MediatorLiveData<List<MedicineEntry>>()
    val meds: LiveData<List<MedicineEntry>> get() = _meds
    private var sourceList: LiveData<List<MedicineEntry>> = MutableLiveData()

    private val medsRepository: MedsRepository = MedsRepository.getInstance(application)

    init {
        getMeds()
    }

    private fun getMeds() = viewModelScope.launch(Dispatchers.Main) {
        _meds.removeSource(sourceList) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(Dispatchers.IO) { sourceList = medsRepository.loadMedicines() }
        _meds.addSource(sourceList) {
            _meds.value = it
        }
    }

    fun insert(medicine: MedicineEntry?) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                medsRepository.insertMedicine(medicine!!)
            }
        }
    }

    fun delete(medicine: MedicineEntry?) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                medsRepository.deleteMedicine(medicine!!)
            }
        }
    }

    fun load(medicineId: Int): LiveData<MedicineEntry> {
        return medsRepository.loadMedicine(medicineId)
    }

    fun update(medicine: MedicineEntry?) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                medsRepository.updateMedicine(medicine!!)
            }
        }
    }
}