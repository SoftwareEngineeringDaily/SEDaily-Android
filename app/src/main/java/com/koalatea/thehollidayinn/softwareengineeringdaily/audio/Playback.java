package com.koalatea.thehollidayinn.softwareengineeringdaily.audio;

import android.support.v4.media.session.MediaSessionCompat;

/**
 * Created by keithholliday on 4/25/18.
 */

public interface Playback {
  /**
   * Start/setup the playback.
   * Resources/listeners would be allocated by implementations.
   */
  void start();

  /**
   * Stop the playback. All resources can be de-allocated by implementations here.
   * @param notifyListeners if true and a callback has been set by setCallback,
   *                        callback.onPlaybackStatusChanged will be called after changing
   *                        the state.
   */
  void stop(boolean notifyListeners);

  /**
   * Set the latest playback state as determined by the caller.
   */
  void setState(int state);

  /**
   * Get the current {@link android.media.session.PlaybackState#getState()}
   */
  int getState();

  /**
   * @return boolean that indicates that this is ready to be used.
   */
  boolean isConnected();

  /**
   * @return boolean indicating whether the player is playing or is supposed to be
   * playing when we gain audio focus.
   */
  boolean isPlaying();

  /**
   * @return pos if currently playing an item
   */
  long getCurrentStreamPosition();

  /**
   * Queries the underlying stream and update the internal last known stream position.
   */
  void updateLastKnownStreamPosition();

  void play(MediaSessionCompat.QueueItem item);

  void pause();

  void seekTo(long position);

  void setCurrentMediaId(String mediaId);

  String getCurrentMediaId();

  void setSpeed(int speed);

  void moveForward(int distance);

  void moveBack(int distance);

  interface Callback {
    /**
     * On current music completed.
     */
    void onCompletion();
    /**
     * on Playback status changed
     * Implementations can use this callback to update
     * playback state on the media sessions.
     */
    void onPlaybackStatusChanged(int state);

    /**
     * @param error to be added to the PlaybackState
     */
    void onError(String error);

    /**
     * @param mediaId being currently played
     */
    void setCurrentMediaId(String mediaId);
  }

  void setCallback(Callback callback);
}
