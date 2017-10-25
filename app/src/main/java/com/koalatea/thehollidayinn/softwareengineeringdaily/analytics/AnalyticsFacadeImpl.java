package com.koalatea.thehollidayinn.softwareengineeringdaily.analytics;

import android.os.Bundle;
import android.support.annotation.NonNull;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by Kurian on 20-Oct-17.
 */
class AnalyticsFacadeImpl implements AnalyticsFacade {

  private final FirebaseAnalytics firebaseAnalytics;

  AnalyticsFacadeImpl(@NonNull FirebaseAnalytics firebaseAnalytics) {
    this.firebaseAnalytics = firebaseAnalytics;
  }

  @Override
  public void trackUpVote(@NonNull String postId) {
    Bundle bundle = new Bundle();
    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, postId);
    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "UP");
    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "VOTE");
    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
  }

  @Override
  public void trackDownVote(@NonNull String postId) {
    Bundle bundle = new Bundle();
    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, postId);
    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "DOWN");
    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "VOTE");
    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
  }

  @Override
  public void trackRegistration(@NonNull String username) {
    Bundle bundle = new Bundle();
    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, username);
    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Register");
    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
  }

  @Override
  public void trackLogin(@NonNull String username) {
    Bundle bundle = new Bundle();
    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, username);
    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Login");
    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
  }
}
