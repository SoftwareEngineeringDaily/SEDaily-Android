package com.koalatea.thehollidayinn.softwareengineeringdaily.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.NetworkModule;

import dagger.Component;

/**
 * Created by Kurian on 25-Sep-17.
 */
@AppScope
@Component(modules = {AppModule.class, NetworkModule.class})
public interface AppComponent {
    Context context();
    SharedPreferences sharedPreferences();
    FirebaseAnalytics firebaseAnalytics();
}
