package com.medsdate;

/**
 * Icon credits Smashicons, Good Ware, smalllikeart, Freepik from flaticon.com
 */

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.stetho.Stetho;
import com.medsdate.data.db.AppDatabase;
import com.medsdate.data.db.MedsRepository;
import com.medsdate.utils.AppExecutors;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class BasicApp extends Application {

    private AppExecutors mAppExecutors;

    @Override
    public void onCreate() {
        super.onCreate();

        CrashlyticsCore core = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());
        Stetho.initializeWithDefaults(this);
        Timber.plant(new Timber.DebugTree());
        mAppExecutors = AppExecutors.getInstance();
    }

    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this, mAppExecutors);
    }

    public MedsRepository getRepository() {
        return MedsRepository.getInstance(getDatabase());
    }
}
