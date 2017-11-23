package com.koalatea.thehollidayinn.softwareengineeringdaily.analytics;

import android.content.Context;
import android.support.annotation.NonNull;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppScope;
import dagger.Module;
import dagger.Provides;

/**
 * Created by Kurian on 20-Oct-17.
 */
@Module
public class AnalyticsModule {


  @Provides
  @AppScope
  FirebaseAnalytics providesFirebaseAnalytics(@NonNull Context context) {
    return FirebaseAnalytics.getInstance(context);
  }

  @Provides
  @AppScope
  AnalyticsFacade providesAnalyticsManager(@NonNull FirebaseAnalytics firebaseAnalytics) {
    return new AnalyticsFacadeImpl(firebaseAnalytics);
  }
}
