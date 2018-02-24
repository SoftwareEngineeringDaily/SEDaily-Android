package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/*
 * Created by keithholliday on 9/30/17.
 */

// @TODO: This should probably be a viewmodal
public class PodcastSessionStateManager {
  private static PodcastSessionStateManager instance = null;

  private final PublishSubject<Integer> speedChangeObservable = PublishSubject.create();
  private final PublishSubject<MediaMetadataCompat> mediaMetaDataChange = PublishSubject.create();

  private final String PROGRESS_KEY = "sedaily-progress-key";
  private final SharedPreferences preferences;
  private Gson gson;

  private String currentTitle = "";
  private long previousSave = 0;
  private long currentProgress = 0;
  private int currentSpeed = 0;
  private MediaMetadataCompat mediaMetadataCompat;
  private Map<String, Long> episodeProgress;

  private PlaybackStateCompat lastPlaybackState;

  private PodcastSessionStateManager() {
    episodeProgress = new HashMap<>();
    preferences = PreferenceManager.getDefaultSharedPreferences(SEDApp.component().context());
    gson = new GsonBuilder().create();
    String progressString = preferences.getString(PROGRESS_KEY, "");
    if (!progressString.isEmpty()) {
      Type typeOfHashMap = new TypeToken<Map<String, Long>>() { }.getType();
      episodeProgress = gson.fromJson(progressString, typeOfHashMap);
    }
  }

  public static PodcastSessionStateManager getInstance() {
    if(instance == null) {
      instance = new PodcastSessionStateManager();
    }
    return instance;
  }

  public long getCurrentProgress() {
      return currentProgress;
  }

  public void setCurrentProgress(long currentProgress) {
      this.currentProgress = currentProgress;
  }

  public PlaybackStateCompat getLastPlaybackState() {
      return lastPlaybackState;
  }

  public void setLastPlaybackState(PlaybackStateCompat lastPlaybackState) {
    this.lastPlaybackState = lastPlaybackState;
  }

  public int getCurrentSpeed() {
    return currentSpeed;
  }

  public void setCurrentSpeed(int currentSpeed) {
    this.currentSpeed = currentSpeed;
    speedChangeObservable.onNext(this.currentSpeed);
  }

  public void setMediaMetaData (MediaMetadataCompat mediaMetaData) {
    this.mediaMetadataCompat = mediaMetaData;
    mediaMetaDataChange.onNext(mediaMetaData);
  }

  public void setProgressForEpisode(String _id, long currentProgress) {
    this.episodeProgress.put(_id, currentProgress);

    // Save every 10 seconds
    long progress = currentProgress/1000;
    if (previousSave == 0) {
      previousSave = progress;
    }

    if (progress - previousSave == 10 && gson != null) {
      previousSave = progress;
      String json = gson.toJson(this.episodeProgress);
      SharedPreferences.Editor editor = preferences.edit();
      editor.putString(PROGRESS_KEY, json);
      editor.apply();
    }

  }

  public long getProgressForEpisode(String _id) {
    if (this.episodeProgress.get(_id) == null) {
      return 0;
    }
    return this.episodeProgress.get(_id);
  }

  public Observable<Integer> getSpeedChanges() {
    return speedChangeObservable;
  }

  public Observable<MediaMetadataCompat> getMetadataChanges() {
    return mediaMetaDataChange;
  }

  public void setCurrentTitle(String title) {
    this.currentTitle = title;
    this.previousSave = 0;
  }

  public String getCurrentTitle() {
    return this.currentTitle;
  }
}
