package com.koalatea.thehollidayinn.softwareengineeringdaily.notifications;

import com.freshchat.consumer.sdk.Freshchat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class SEDFirebaseMessagingService extends FirebaseMessagingService {
  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    if (Freshchat.isFreshchatNotification(remoteMessage)) {
      Freshchat.getInstance(this).handleFcmMessage(remoteMessage);
    } else {
      //Handle notifications with data payload for your app
    }
  }
}
