package com.koalatea.thehollidayinn.softwareengineeringdaily.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Kurian on 25-Sep-17.
 */
@Module
public class AppModule {

    private final SEDApp app;

    public AppModule(@NonNull SEDApp app) {
        this.app = app;
    }

    @Provides
    @Singleton
    Application providesApplication() {
        return this.app;
    }

    @Provides
    @Singleton
    Context providesContext() {
        return this.app;
    }

    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
