package com.koalatea.thehollidayinn.softwareengineeringdaily.analytics;

import android.content.Context;
import android.support.annotation.NonNull;
import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Kurian on 20-Oct-17.
 */
@Module
public class AnalyticsModule {

  @Provides
  @Singleton
  FirebaseAnalytics providesFirebaseAnalytics(@NonNull Context context) {
    return FirebaseAnalytics.getInstance(context);
  }

  @Provides
  @Singleton
  AnalyticsFacade providesAnalyticsManager(@NonNull FirebaseAnalytics firebaseAnalytics) {
    return new AnalyticsFacadeImpl(firebaseAnalytics);
  }
}
