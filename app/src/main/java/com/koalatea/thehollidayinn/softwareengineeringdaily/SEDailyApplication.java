package com.koalatea.thehollidayinn.softwareengineeringdaily;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/*
 * Created by keithholliday on 10/7/17.
 */

public class SEDailyApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();

    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    LeakCanary.install(this);
  }
}
