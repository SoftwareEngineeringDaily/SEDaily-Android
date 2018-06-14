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
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.AppDatabase;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Download;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.BookmarkDao;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.DownloadDao;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.DownloadRepository;
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
  private static PodcastDownloadsRepository instance = null;

  private Map<String, Boolean> filesLoaded = new HashMap<>();
  private Map<String, Boolean> downloading = new HashMap<>();
  private final PublishSubject<String> changeObservable = PublishSubject.create();
  private int currentDownloadId;
  private DownloadNotificationManager downloadNotificationManager;
  private DownloadRepository downloadRepository;

  private PodcastDownloadsRepository() {
    PRDownloader.initialize(SEDApp.component.context());
    downloadNotificationManager = DownloadNotificationManager.getInstance();
    downloadRepository = new DownloadRepository();
  }

  public static PodcastDownloadsRepository getInstance() {
    if(instance == null) {
      instance = new PodcastDownloadsRepository();
    }
    return instance;
  }

  // @TOOD: We have so many states
  public void setPodcastDownload(String podcastId) {
    this.filesLoaded.put(podcastId, true);
    this.downloading.put(podcastId, false);
    changeObservable.onNext(podcastId);

    Download download = new Download(podcastId);
    downloadRepository.insert(download);
  }

  public void removePodcastDownload(String podcastId) {
    this.downloading.put(podcastId, false);
    this.filesLoaded.put(podcastId, false);
    changeObservable.onNext(podcastId);
    downloadRepository.remove(podcastId);
  }

  public Boolean isDownloading(String podcastId) {
    if (this.downloading.get(podcastId) != null) {
      return this.downloading.get(podcastId);
    }
    return false;
  }

  public Boolean isPodcastDownloaded(Post post) {
    String postID = post.get_id();
    Boolean fileForId = this.filesLoaded.get(postID);

    if (fileForId != null && fileForId) return fileForId;

    String mp3 = post.getMp3();
    if (mp3 == null ||mp3.isEmpty()) {
      return false;
    }

    MP3FileManager mp3FileManager = new MP3FileManager();
    Context context = SEDApp.component.context();
    String filename = mp3FileManager.getFileNameFromUrl(mp3);
    File file = new File(mp3FileManager.getRootDirPath(context), filename);

    if (file.exists()) {
      this.filesLoaded.put(postID, true);
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
    String mp3 = post.getMp3();
    String file = mp3FileManager.getFileNameFromUrl(mp3);

    setPodcastDownload(post.get_id());

    String directory = mp3FileManager.getRootDirPath(context);

    currentDownloadId = PRDownloader
      .download(mp3, directory, file)
      .build()
      .start(new OnDownloadListener() {
        @Override
        public void onDownloadComplete() {
          downloadNotificationManager.hideNotification(currentDownloadId);
        }

        @Override
        public void onError(Error error) {
          downloadNotificationManager.hideNotification(currentDownloadId);
        }
      });

    downloadNotificationManager.showDownloadNotification(currentDownloadId, post);
  }



  public void removeFileForPost (Post post) {
    String mp3 = post.getMp3();

    if (mp3 == null || mp3.isEmpty()) {
      return;
    }

    Context appContext = SEDApp.component().context();
    MP3FileManager mp3FileManager = new MP3FileManager();

    File file = mp3FileManager.getFileFromUrl(mp3, appContext);
    file.delete();

    removePodcastDownload(post.get_id());
  }

  public void cancelDownload (Post post) {
    this.downloading.put(post.get_id(), false);
    removeFileForPost(post);
    PRDownloader.cancel(currentDownloadId);
    downloadNotificationManager.cancelNotification();
  }
}
