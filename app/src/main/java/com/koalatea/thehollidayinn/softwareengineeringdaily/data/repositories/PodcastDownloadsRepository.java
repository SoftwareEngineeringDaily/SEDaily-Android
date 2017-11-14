package com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories;

import android.app.Application;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppModule;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SDEApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.downloads.MP3FileManager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by keithholliday on 11/3/17.
 */

public class PodcastDownloadsRepository {
  private static PodcastDownloadsRepository instance = null;

  private Map<String, Boolean> filesLoaded = new HashMap<>();
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
    Timber.v("keithtest2-setPodcastDownload");
    changeObservable.onNext(podcastId);
  }

  public void removePodcastDownload(String podcastId) {
    this.filesLoaded.put(podcastId, false);
    Timber.v("keithtest2-removePodcastDownload");
    changeObservable.onNext(podcastId);
  }

  public Boolean isPodcastDownloaded(Post post) {
    if (this.filesLoaded.get(post.getId()) != null && this.filesLoaded.get(post.getId())) return true;

    if (post.getMp3() == null || post.getMp3().isEmpty()) {
      return false;
    }

    File file = new MP3FileManager().getFileFromUrl(post.getMp3(), SDEApp.component().context());
    if (file.exists()) {
      this.filesLoaded.put(post.getId(), true);
      return true;
    }

    return false;
  }

  public Observable<String> getDownloadChanges() {
    return changeObservable;
  }
}
