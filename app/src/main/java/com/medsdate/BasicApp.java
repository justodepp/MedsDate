package com.medsdate;

import android.app.Application;

import com.medsdate.data.db.AppDatabase;
import com.medsdate.data.db.MedsRepository;
import com.medsdate.utils.AppExecutors;

public class BasicApp extends Application {

    private AppExecutors mAppExecutors;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppExecutors = AppExecutors.getInstance();
    }

    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this, mAppExecutors);
    }

    public MedsRepository getRepository() {
        return MedsRepository.getInstance(getDatabase());
    }
}
