package com.koalatea.thehollidayinn.softwareengineeringdaily.notifications;

import com.freshchat.consumer.sdk.Freshchat;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class SEDFirebaseInstanceIdService extends FirebaseInstanceIdService {
  @Override
  public void onTokenRefresh() {
    String token = FirebaseInstanceId.getInstance().getToken();
    Freshchat.getInstance(this).setPushRegistrationToken(token);
  }
}
