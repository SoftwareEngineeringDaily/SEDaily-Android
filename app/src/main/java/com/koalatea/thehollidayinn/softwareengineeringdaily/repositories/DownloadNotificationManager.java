package com.koalatea.thehollidayinn.softwareengineeringdaily.repositories;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppComponent;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;

public class DownloadNotificationManager {
  private static DownloadNotificationManager instance = null;

  private NotificationManager mNotifyManager;
  private NotificationCompat.Builder mBuilder;

  public static DownloadNotificationManager getInstance() {
    if(instance == null) {
      instance = new DownloadNotificationManager();
    }
    return instance;
  }

  public void showDownloadNotification(int downloadId, Post post) {
    AppComponent app = SEDApp.component();
    Context context = app.context();

    mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    String CHANNEL_ID = "sedaily_player_notifications";
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = context.getString(R.string.app_name);
      int importance = NotificationManager.IMPORTANCE_LOW;

      NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

      mNotifyManager.createNotificationChannel(mChannel);
    }

    String postTitle =  post.getTitle().getRendered();

    mBuilder =
      new NotificationCompat.Builder(context, CHANNEL_ID)
          .setContentTitle("Downloading " + postTitle)
          .setContentText("Download in progress")
          .setSmallIcon(R.drawable.sedaily_logo);

    mNotifyManager.notify(downloadId, mBuilder.build());
  }

  public void hideNotification(int download) {
    mBuilder.setContentText("Download complete")
            .setProgress(0,0,false);
    mNotifyManager.notify(download, mBuilder.build());
  }

  public void cancelNotification () {
    mNotifyManager.cancel(1);
  }
}
