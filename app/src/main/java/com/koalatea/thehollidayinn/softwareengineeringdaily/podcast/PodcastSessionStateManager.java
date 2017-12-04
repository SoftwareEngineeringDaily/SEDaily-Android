package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.support.v4.media.session.PlaybackStateCompat;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/*
 * Created by keithholliday on 9/30/17.
 */

public class PodcastSessionStateManager {
  private static PodcastSessionStateManager instance = null;
  private final PublishSubject<Integer> speedChangeObservable = PublishSubject.create();

  private long currentProgress = 0;
  private int currentSpeed = 0;

  private PlaybackStateCompat lastPlaybackState;

  private PodcastSessionStateManager() {}

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

  public Observable<Integer> getSpeedChanges() {
    return speedChangeObservable;
  }
}
