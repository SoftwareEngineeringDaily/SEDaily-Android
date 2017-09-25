package com.koalatea.thehollidayinn.softwareengineeringdaily.app;

import android.app.Application;

import com.koalatea.thehollidayinn.softwareengineeringdaily.BuildConfig;

import timber.log.Timber;

/**
 * Created by Kurian on 25-Sep-17.
 */

public class SDEApp extends Application {

    static AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        initDependencies();
        createLogger();
    }

    private void createLogger() {
        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void initDependencies() {
        if(component != null) {
            component = DaggerAppComponent.builder()
                    .appModule(new AppModule(this))
                    .build();
        }
    }

    public static AppComponent component() {
        return component;
    }
}
