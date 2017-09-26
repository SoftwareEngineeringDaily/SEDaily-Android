package com.koalatea.thehollidayinn.softwareengineeringdaily.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.koalatea.thehollidayinn.softwareengineeringdaily.utils.LocalTextUtils;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Kurian on 25-Sep-17.
 */
@Module
public class AppModule {

    private final SDEApp app;

    public AppModule(@NonNull SDEApp app) {
        this.app = app;
    }

    @Provides
    @AppScope
    Application providesApplication() {
        return this.app;
    }

    @Provides
    @AppScope
    Context providesContext() {
        return this.app;
    }

    @Provides
    @AppScope
    SharedPreferences providesSharedPreferences(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @AppScope
    FirebaseAnalytics providesFirebaseAnalytics(@NonNull Context context) {
        //TODO wrap this in a platform-independent wrapper
        return FirebaseAnalytics.getInstance(context);
    }

    @Provides
    @AppScope
    LocalTextUtils providesLocalTextUtils() {
        return new LocalTextUtils();
    }
}
