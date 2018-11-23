package com.medsdate.ui.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import com.medsdate.BasicApp;
import com.medsdate.data.db.MedsRepository;
import com.medsdate.data.db.model.MedicineEntry;

public class MedicineViewModel extends AndroidViewModel {

    private final LiveData<MedicineEntry> mObservableMedicine;

    public ObservableField<MedicineEntry> medicine = new ObservableField<>();

    private final int mMedicineId;

    public MedicineViewModel(@NonNull Application application, MedsRepository repository,
                            final int medicineId) {
        super(application);
        mMedicineId = medicineId;

        mObservableMedicine = repository.loadMedicine(mMedicineId);
    }

    public LiveData<MedicineEntry> getObservableMedicine() {
        return mObservableMedicine;
    }

    public void setMedicine(MedicineEntry medicine) {
        this.medicine.set(medicine);
    }

    /**
     * A creator is used to inject the medicine ID into the ViewModel
     * <p>
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the medicine ID can be passed in a public method.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final int mMedicineId;

        private final MedsRepository mRepository;

        public Factory(@NonNull Application application, int medicineId) {
            mApplication = application;
            mMedicineId = medicineId;
            mRepository = ((BasicApp) application).getRepository();
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new MedicineViewModel(mApplication, mRepository, mMedicineId);
        }
    }
}
