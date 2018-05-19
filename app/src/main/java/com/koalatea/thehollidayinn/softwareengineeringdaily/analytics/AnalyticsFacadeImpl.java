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

  private void trackFirebaseBasic(String itemId, String name, String contentType) {
    Bundle bundle = new Bundle();
    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
  }

  @Override
  public void trackUpVote(@NonNull String postId) {
    trackFirebaseBasic(postId, "UP", "VOTE");
  }

  @Override
  public void trackDownVote(@NonNull String postId) {
    trackFirebaseBasic(postId, "DOWN", "VOTE");
  }

  @Override
  public void trackRegistration(@NonNull String username) {
    trackFirebaseBasic(username, "Register", "Register");
  }

  @Override
  public void trackLogin(@NonNull String username) {
    trackFirebaseBasic(username, "Login", "Login");
  }
}
