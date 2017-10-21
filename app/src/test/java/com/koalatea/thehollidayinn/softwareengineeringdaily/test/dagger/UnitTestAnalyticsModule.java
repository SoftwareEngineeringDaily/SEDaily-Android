package com.koalatea.thehollidayinn.softwareengineeringdaily.test.dagger;

import com.koalatea.thehollidayinn.softwareengineeringdaily.analytics.AnalyticsFacade;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppScope;
import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

/**
 * Created by Kurian on 20-Oct-17.
 */
@Module
public class UnitTestAnalyticsModule {

  @Provides
  @AppScope
  AnalyticsFacade providesAnalyticsManager() {
    return mock(AnalyticsFacade.class);
  }
}
