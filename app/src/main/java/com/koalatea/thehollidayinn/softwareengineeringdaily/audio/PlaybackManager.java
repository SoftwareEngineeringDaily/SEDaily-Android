package com.koalatea.thehollidayinn.softwareengineeringdaily.audio;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Created by keithholliday on 4/29/18.
 */

public class PlaybackManager implements Playback.Callback {
  private static final String TAG = "PlaybackManager";

  private MusicProvider mMusicProvider;
  private QueueManager mQueueManager;
  private Resources mResources;
  private Playback mPlayback;
  private PlaybackServiceCallback mServiceCallback;
  private MediaSessionCallback mMediaSessionCallback;

  public PlaybackManager(PlaybackServiceCallback serviceCallback, Resources resources,
                         MusicProvider musicProvider, QueueManager queueManager,
                         Playback playback) {
    mMusicProvider = musicProvider;
    mServiceCallback = serviceCallback;
    mResources = resources;
    mQueueManager = queueManager;
    mMediaSessionCallback = new MediaSessionCallback();
    mPlayback = playback;
    mPlayback.setCallback(this);
  }

  public Playback getPlayback() {
    return mPlayback;
  }

  public MediaSessionCompat.Callback getMediaSessionCallback() {
    return mMediaSessionCallback;
  }

  public void handlePlayRequest() {
    MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
    if (currentMusic == null) return;
    mServiceCallback.onPlaybackStart();
    mPlayback.play(currentMusic);
  }

  public void handlePauseRequest() {
    if (!mPlayback.isPlaying()) return;
    mPlayback.pause();
    mServiceCallback.onPlaybackStop();
  }

  public void handleStopRequest(String withError) {
    mPlayback.stop(true);
    mServiceCallback.onPlaybackStop();
    updatePlaybackState(withError);
  }

  public void updatePlaybackState(String error) {
    long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;

    if (mPlayback != null && mPlayback.isConnected()) {
      position = mPlayback.getCurrentStreamPosition();
    }

    PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(getAvailableActions());

    setCustomAction(stateBuilder);

    int state = mPlayback.getState();

    if (error != null) {
      stateBuilder.setErrorMessage(error);
      state = PlaybackStateCompat.STATE_ERROR;
    }

    stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

    MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
    if (currentMusic != null) {
      stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
    }

    mServiceCallback.onPlaybackStateUpdated(stateBuilder.build());

    if (state == PlaybackStateCompat.STATE_PLAYING ||
            state == PlaybackStateCompat.STATE_PAUSED) {
      mServiceCallback.onNotificationRequired();
    }
  }

  private void setCustomAction(PlaybackStateCompat.Builder stateBuilder) {
    MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();

    if (currentMusic == null) return;

    String mediaId = currentMusic.getDescription().getMediaId();
    if (mediaId == null) return;
  }

  private long getAvailableActions() {
    long actions =
            PlaybackStateCompat.ACTION_PLAY_PAUSE |
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT;

    if (mPlayback.isPlaying()) {
      actions |= PlaybackStateCompat.ACTION_PAUSE;
    } else {
      actions |= PlaybackStateCompat.ACTION_PLAY;
    }

    return actions;
  }

  @Override
  public void onCompletion() {
    if (mQueueManager.skipQueuePosition(1)) {
      handlePlayRequest();
      mQueueManager.updateMetadata();
    } else {
      handleStopRequest(null);
    }
  }

  @Override
  public void onPlaybackStatusChanged(int state) {
    updatePlaybackState(null);
  }

  @Override
  public void onError(String error) {
    updatePlaybackState(error);
  }

  @Override
  public void setCurrentMediaId(String mediaId) {
    mQueueManager.setQueueFromMusic(mediaId);
  }

  private class MediaSessionCallback extends MediaSessionCompat.Callback {
    @Override
    public void onPlay() {
      if (mQueueManager.getCurrentMusic() == null) {
        mQueueManager.setRandomQueue();
      }
      handlePlayRequest();
    }

    @Override
    public void onSkipToQueueItem(long queueId) {
      mQueueManager.setCurrentQueueItem(queueId);
      mQueueManager.updateMetadata();
    }

    @Override
    public void onSeekTo(long position) {
      mPlayback.seekTo((int) position);
    }

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
      mQueueManager.setQueueFromMusic(mediaId);
      handlePlayRequest();
    }

    @Override
    public void onPause() {
      handlePauseRequest();
    }

    @Override
    public void onStop() {
      handleStopRequest(null);
    }

    @Override
    public void onSkipToNext() {
      if (mQueueManager.skipQueuePosition(1)) {
        handlePlayRequest();
      } else {
        handleStopRequest("Cannot skip");
      }

      mQueueManager.updateMetadata();
    }

    @Override
    public void onSkipToPrevious() {
      if (mQueueManager.skipQueuePosition(-1)) {
        handlePlayRequest();
      } else {
        handleStopRequest("Cannot skip");
      }

      mQueueManager.updateMetadata();
    }

    @Override
    public void onCustomAction (String action,
                                Bundle extras) {
      if (action.equals("SPEED_CHANGE")) {
        int speed = extras.getInt("SPEED");
        mPlayback.setSpeed(speed);
      } else if (action.equals("MOVE_BACK")) {
        int speed = extras.getInt("DISTANCE");
        mPlayback.moveBack(speed);
      } else if (action.equals("MOVE_FORWARD")) {
        int speed = extras.getInt("DISTANCE");
        mPlayback.moveForward(speed);
      }
    }

    // @TODO: https://github.com/googlesamples/android-UniversalMusicPlayer/blob/master/mobile/src/main/java/com/example/android/uamp/playback/PlaybackManager.java#L329
  }

  public interface PlaybackServiceCallback {
    void onPlaybackStart();

    void onNotificationRequired();

    void onPlaybackStop();

    void onPlaybackStateUpdated(PlaybackStateCompat newState);
  }
}
