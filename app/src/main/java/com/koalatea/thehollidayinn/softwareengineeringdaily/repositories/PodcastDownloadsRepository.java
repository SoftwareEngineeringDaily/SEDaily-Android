package com.koalatea.thehollidayinn.softwareengineeringdaily.repositories;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppComponent;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.downloads.MP3FileManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

/*
 * Created by keithholliday on 11/3/17.
 */

public class PodcastDownloadsRepository {
  private NotificationManager mNotifyManager;
  private NotificationCompat.Builder mBuilder;

  private static PodcastDownloadsRepository instance = null;

  private Map<String, Boolean> filesLoaded = new HashMap<>();
  private Map<String, Boolean> downloading = new HashMap<>();
  private final PublishSubject<String> changeObservable = PublishSubject.create();

  private int currentDownloadId;

  private PodcastDownloadsRepository() {
    PRDownloader.initialize(SEDApp.component.context());
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

    MP3FileManager mp3FileManager = new MP3FileManager();
    Context context = SEDApp.component.context();
    String filename = mp3FileManager.getFileNameFromUrl(post.getMp3());
    File file = new File(mp3FileManager.getRootDirPath(context), filename);

    if (file.exists()) {
      this.filesLoaded.put(post.get_id(), true);
      return true;
    }

    return false;
  }

  public Observable<String> getDownloadChanges() {
    return changeObservable;
  }

  public void downloadPostMP3(Post post) {
    Context context = SEDApp.component.context();
    MP3FileManager mp3FileManager = new MP3FileManager();
    String file = mp3FileManager.getFileNameFromUrl(post.getMp3());

    setPodcastDownload(post.get_id());

    currentDownloadId = PRDownloader
      .download(post.getMp3(), mp3FileManager.getRootDirPath(context), file)
      .build()
      .start(new OnDownloadListener() {
        @Override
        public void onDownloadComplete() {
          hideNotification(currentDownloadId);
        }

        @Override
        public void onError(Error error) {
          hideNotification(currentDownloadId);
        }
      });

    showDownloadNotification(currentDownloadId, post);
  }

  private void hideNotification(int download) {
    mBuilder.setContentText("Download complete")
            .setProgress(0,0,false);
    mNotifyManager.notify(download, mBuilder.build());
  }

  private void showDownloadNotification(int downloadId, Post post) {
    AppComponent app = SEDApp.component();

    mNotifyManager =
            (NotificationManager) app.context().getSystemService(Context.NOTIFICATION_SERVICE);

    String CHANNEL_ID = "sedaily_player_notifications";

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = app.context().getString(R.string.app_name);
      int importance = NotificationManager.IMPORTANCE_LOW;
      NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
      mNotifyManager.createNotificationChannel(mChannel);
    }

    mBuilder =
      new NotificationCompat.Builder(app.context(), CHANNEL_ID)
        .setContentTitle("Downloading " + post.getTitle().getRendered())
        .setContentText("Download in progress")
        .setSmallIcon(R.drawable.sedaily_logo);

    mNotifyManager.notify(downloadId, mBuilder.build());
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
    PRDownloader.cancel(currentDownloadId);
    mNotifyManager.cancel(1);
  }
}
