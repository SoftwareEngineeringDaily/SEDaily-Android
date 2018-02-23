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
import com.koalatea.thehollidayinn.softwareengineeringdaily.downloads.DownloadTask;
import com.koalatea.thehollidayinn.softwareengineeringdaily.downloads.MP3FileManager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by keithholliday on 11/3/17.
 */

public class PodcastDownloadsRepository {
  private DownloadTask downloadTask;
  private NotificationManager mNotifyManager;

  private static PodcastDownloadsRepository instance = null;

  private Map<String, Boolean> filesLoaded = new HashMap<>();
  private Map<String, Boolean> downloading = new HashMap<>();
  private final PublishSubject<String> changeObservable = PublishSubject.create();

  private PodcastDownloadsRepository() {
  }

  public static PodcastDownloadsRepository getInstance() {
    if(instance == null) {
      instance = new PodcastDownloadsRepository();
    }
    return instance;
  }

  public void setPodcastDownload(String podcastId) {
    this.filesLoaded.put(podcastId, true);
    this.downloading.put(podcastId, false);
    changeObservable.onNext(podcastId);
  }

  public void removePodcastDownload(String podcastId) {
    this.downloading.put(podcastId, false);
    this.filesLoaded.put(podcastId, false);
    changeObservable.onNext(podcastId);
  }

  public Boolean isDownloading(String podcastId) {
    if (this.downloading.get(podcastId) != null) {
      return this.downloading.get(podcastId);
    }
    return false;
  }

  public Boolean isPodcastDownloaded(Post post) {
    if (this.filesLoaded.get(post.get_id()) != null && this.filesLoaded.get(post.get_id())) return this.filesLoaded.get(post.get_id());

    if (post.getMp3() == null || post.getMp3().isEmpty()) {
      return false;
    }

    File file = new MP3FileManager().getFileFromUrl(post.getMp3(), SEDApp.component().context());
    if (file.exists()) {
      this.filesLoaded.put(post.get_id(), true);
      return true;
    }

    return false;
  }

  public Observable<String> getDownloadChanges() {
    return changeObservable;
  }

  public void displayDownloadNotification(Post post) {
    AppComponent app = SEDApp.component();

    final int id = 1;
    mNotifyManager =
        (NotificationManager) app.context().getSystemService(Context.NOTIFICATION_SERVICE);

    String CHANNEL_ID = "sedaily_player_notifications";
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = app.context().getString(R.string.app_name);
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
      mNotifyManager.createNotificationChannel(mChannel);
    }

    final NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(app.context(), CHANNEL_ID)
            .setContentTitle("Downloading " + post.getTitle().getRendered())
            .setContentText("Download in progress")
            .setSmallIcon(R.drawable.sedaily_logo);

    mNotifyManager.notify(id, mBuilder.build());

    // execute this when the downloader must be fired
    downloadTask = new DownloadTask(mNotifyManager, mBuilder, post.get_id());
    downloadTask.execute(post.getMp3());
    this.downloading.put(post.get_id(), true);
  }

  public void removeFileForPost (Post post) {
    if (post.getMp3() == null || post.getMp3().isEmpty()) {
      return;
    }

    File file = new MP3FileManager().getFileFromUrl(post.getMp3(), SEDApp.component().context());
    file.delete();

    removePodcastDownload(post.get_id());
  }

  public void cancelDownload (Post post) {
    this.downloading.put(post.get_id(), false);
    removeFileForPost(post);
    downloadTask.cancel(true);
    mNotifyManager.cancel(1);
  }
}
