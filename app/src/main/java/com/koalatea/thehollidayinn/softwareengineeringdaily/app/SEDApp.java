package com.koalatea.thehollidayinn.softwareengineeringdaily.app;

import android.app.Application;
import android.support.annotation.VisibleForTesting;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.koalatea.thehollidayinn.softwareengineeringdaily.BuildConfig;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.AppDatabase;
import com.squareup.leakcanary.LeakCanary;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Kurian on 25-Sep-17.
 */

public class SEDApp extends Application {

  @VisibleForTesting
  public static AppComponent component;

  @Override
  public void onCreate() {
    super.onCreate();

    CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
            .setDefaultFontPath("Roboto-RobotoRegular.ttf")
            .setFontAttrId(R.attr.fontPath)
            .build()
    );

    initLeakCanary();
    initDependencies();
    createLogger();
    // Enable RxJava assembly stack collection, to make RxJava crash reports clear and unique
    // Make sure this is called AFTER setting up any Crash reporting mechanism as Crashlytics
    RxJava2Debug.enableRxJava2AssemblyTracking(new String[] { BuildConfig.APPLICATION_ID });
  }

  private void initLeakCanary() {
    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    LeakCanary.install(this);
  }

  private void createLogger() {
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }
  }

  private void initDependencies() {
    if (component == null) {
      component = DaggerAppComponent.builder()
              .appModule(new AppModule(this))
              .build();
    }
  }

  public static AppComponent component() {
    return component;
  }
}
