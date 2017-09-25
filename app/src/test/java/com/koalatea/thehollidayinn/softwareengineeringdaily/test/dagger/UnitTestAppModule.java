package com.koalatea.thehollidayinn.softwareengineeringdaily.test.dagger;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.mock.MockContext;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppScope;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Created by Kurian on 25-Sep-17.
 */
@Module
class UnitTestAppModule {

    @Provides
    @AppScope
    Context providesContext() {
        return spy(new MockContext());
    }

    @Provides
    @AppScope
    SharedPreferences providesPreferences() {
        return mock(SharedPreferences.class);
    }

    @Provides
    @AppScope
    FirebaseAnalytics providesFirebaseAnalytics() {
        return null;
    }
}
