package com.koalatea.thehollidayinn.softwareengineeringdaily.audio;

/*
  Created by krh12 on 6/16/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.crash.FirebaseCrash;
import com.koalatea.thehollidayinn.softwareengineeringdaily.podcast.PodcastSessionStateManager;

import static com.google.android.exoplayer2.C.CONTENT_TYPE_SPEECH;
import static com.google.android.exoplayer2.C.USAGE_MEDIA;


/**
 * A class that implements local media playback using {@link android.media.MediaPlayer}
 */

public class LocalPlayback implements Playback {

    private static final String TAG = LocalPlayback.class.getSimpleName();

    // The volume we set the media player to when we lose audio focus, but are
    // allowed to reduce the volume instead of stopping playback.
    private static final float VOLUME_DUCK = 0.2f;
    // The volume we set the media player when we have audio focus.
    private static final float VOLUME_NORMAL = 1.0f;

    // we don't have audio focus, and can't duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    // we don't have focus, but can duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    // we have full audio focus
    private static final int AUDIO_FOCUSED = 2;

    private final Context mContext;
    private final WifiManager.WifiLock mWifiLock;
    private boolean mPlayOnFocusGain;
    private Callback mCallback;
    private final MusicProvider mMusicProvider;
    private boolean mAudioNoisyReceiverRegistered;
    private volatile String mCurrentMediaId;

    private int mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
    private final AudioManager mAudioManager;
    private SimpleExoPlayer mExoPlayer;
    private final ExoPlayerEventListener mEventListener = new ExoPlayerEventListener();

    // Whether to return STATE_NONE or STATE_STOPPED when mExoPlayer is null;
    private boolean mExoPlayerNullIsStopped =  false;

    private final IntentFilter mAudioNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private final BroadcastReceiver mAudioNoisyReceiver =
    new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (!AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) return;

        if (!isPlaying()) return;

        Intent i = new Intent(context, MusicService.class);
        i.setAction(MusicService.ACTION_CMD);
        i.putExtra(MusicService.CMD_NAME, MusicService.CMD_PAUSE);
        mContext.startService(i);
      }
    };

    // Make a class or something else?
    private String currentSource = "";
    private MediaSessionCompat.QueueItem currentQueueItem;
    private String currentMediaId;
    private Handler handler;
    private HandlerThread mHandlerThread;
    private final Runnable updateProgressAction = () -> updateProgressBar();

    public LocalPlayback(Context context, MusicProvider musicProvider) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mMusicProvider = musicProvider;


        this.mAudioManager = (AudioManager) applicationContext.getSystemService(Context.AUDIO_SERVICE);

        // Create the Wifi lock (this does not acquire the lock, this just creates it).
        this.mWifiLock = ((WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "sample_lock");

        startHandlerThread();
    }

    public void startHandlerThread(){
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        handler = new Handler(mHandlerThread.getLooper());
    }

  @Override
  public void start() {}

  @Override
  public void stop(boolean notifiyListeners) {
    giveUpAudioFocus();
    unregisterAudioNoisyReceiver();
    releaseResources(true);
  }

  @Override
  public void setState(int state) {}

  @Override
  public int getState() {
    if (mExoPlayer == null) {
      return mExoPlayerNullIsStopped
              ? PlaybackStateCompat.STATE_STOPPED
              : PlaybackStateCompat.STATE_NONE;
    }

    switch (mExoPlayer.getPlaybackState()) {
      case ExoPlayer.STATE_IDLE:
        return PlaybackStateCompat.STATE_PAUSED;
      case ExoPlayer.STATE_BUFFERING:
        return PlaybackStateCompat.STATE_BUFFERING;
      case ExoPlayer.STATE_READY:
        return mExoPlayer.getPlayWhenReady()
                ? PlaybackStateCompat.STATE_PLAYING
                : PlaybackStateCompat.STATE_PAUSED;
      case ExoPlayer.STATE_ENDED:
        return PlaybackStateCompat.STATE_PAUSED;
      default:
        return PlaybackStateCompat.STATE_NONE;
    }
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public boolean isPlaying() {
    return mPlayOnFocusGain || (mExoPlayer != null && mExoPlayer.getPlayWhenReady());
  }

  @Override
  public long getCurrentStreamPosition() {
    return mExoPlayer != null ? mExoPlayer.getCurrentPosition() : 0;
  }

  @Override
  public void updateLastKnownStreamPosition() {

  }

  @Override
  public void play(MediaSessionCompat.QueueItem item) {
    mPlayOnFocusGain = true;
    tryToGetAudioFocus();
    registerAudioNoisyReceiver();

    currentQueueItem = item;
    String mediaId = item.getDescription().getMediaId();
    currentMediaId = mediaId;
    boolean mediaHasChanged = !TextUtils.equals(mediaId, mCurrentMediaId);
    if (mediaHasChanged) {
        mCurrentMediaId = mediaId;
    }

    if (mediaHasChanged || mExoPlayer == null) {
      releaseResources(false);

      // Do we need this? I think it ensures we have the latest data
      MediaMetadataCompat track = mMusicProvider.getMusic(mediaId);
      if (track == null) return;

      String source = track.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
      if (source != null) {
        source = source.replaceAll(" ", "%20"); // Escape spaces for URLs
      }
      currentSource = source;

      if (mExoPlayer == null) {
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector(), new DefaultLoadControl());
        mExoPlayer.addListener(mEventListener);
      }

      final AudioAttributes audioAttributes = new AudioAttributes.Builder()
              .setContentType(CONTENT_TYPE_SPEECH) // For podcasts
              .setUsage(USAGE_MEDIA)
              .build();
      mExoPlayer.setAudioAttributes(audioAttributes);

      // @TODO: Here we use uamp. I think that need to be something else
      DataSource.Factory dataSourceFactory =
        new DefaultDataSourceFactory(
          mContext,
          Util.getUserAgent(mContext, "uamp"),
          null
        );

      ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

      MediaSource mediaSource = new ExtractorMediaSource(
        Uri.parse(source),
        dataSourceFactory,
        extractorsFactory,
        null,
        null
      );

      // Prepares media to play (happens on background thread) and triggers
      // {@code onPlayerStateChanged} callback when the stream is ready to play.
      mExoPlayer.prepare(mediaSource);

      // If we are streaming from the internet, we want to hold a
      // Wifi lock, which prevents the Wifi radio from going to
      // sleep while the song is playing.
      mWifiLock.acquire();
    }

    configurePlayerState();
  }

  private void setDuration() {
    long duration = mExoPlayer.getDuration();
    String source = currentSource;

    if (currentMediaId == null || currentSource.isEmpty() || currentQueueItem == null) return;

    // @TODO: This should be perpared somehwere else. But mayeb we need the exploplay to get the duration
    MediaMetadataCompat updatedItem = new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, currentMediaId)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, source)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentQueueItem.getDescription().getTitle().toString())
            .build();

    mMusicProvider.updateMusic(currentMediaId, updatedItem);
    PodcastSessionStateManager.getInstance().setMediaMetaData(updatedItem);
  }

  @Override
  public void pause() {
    if (mExoPlayer != null) {
      mExoPlayer.setPlayWhenReady(false);
    }

    releaseResources(false);
    unregisterAudioNoisyReceiver();
  }

  @Override
  public void seekTo(long position) {
    if (mExoPlayer == null) return;

    registerAudioNoisyReceiver();
    mExoPlayer.seekTo(position);
  }

  public void moveForward(int distance) {
    if (mExoPlayer == null) return;
    long currentPos = mExoPlayer.getCurrentPosition();
    this.seekTo(currentPos + distance);
  }

  public void moveBack(int distance) {
    if (mExoPlayer == null) return;
    long currentPos = mExoPlayer.getCurrentPosition();
    this.seekTo(currentPos - distance);
  }

  public void setSpeed(int speed) {
    float speedFloat = 1f;

    if (speed == 1) {
      speedFloat = 1.5f;
    } else if (speed == 2) {
      speedFloat = 2f;
    }

    try {
      PlaybackParameters current = mExoPlayer.getPlaybackParameters();
      PlaybackParameters newParams = new PlaybackParameters(speedFloat, current.pitch);
      mExoPlayer.setPlaybackParameters(newParams);
    } catch (Exception e) {
        FirebaseCrash.report(new Exception(e));
    }
  }

  @Override
  public void setCallback(Callback callback) {
    this.mCallback = callback;
  }

  @Override
  public void setCurrentMediaId(String mediaId) {
    this.mCurrentMediaId = mediaId;
  }

  @Override
  public String getCurrentMediaId() {
    return mCurrentMediaId;
  }

  /**
   * Try to get the system audio focus.
   */
  private void tryToGetAudioFocus() {
      int result = mAudioManager.requestAudioFocus(
        mOnAudioFocusChangeListener,
        AudioManager.STREAM_MUSIC,
        AudioManager.AUDIOFOCUS_GAIN
      );

      if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
        mCurrentAudioFocusState = AUDIO_FOCUSED;
        return;
      }

      mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
  }

  /**
   * Give up the audio focus.
   */
  private void giveUpAudioFocus() {
    if (mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener)
            == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
    }
  }

  /**
   * Reconfigures the player according to audio focus settings and starts/restarts it. This method
   * starts/restarts the ExoPlayer instance respecting the current audio focus state. So if we
   * have focus, it will play normally; if we don't have focus, it will either leave the player
   * paused or set it to a low volume, depending on what is permitted by the current focus
   * settings.
   */
  private void configurePlayerState() {
    if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_NO_DUCK) {
      pause();
      return;
    }

    registerAudioNoisyReceiver();

    if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_CAN_DUCK) {
      mExoPlayer.setVolume(VOLUME_DUCK);
    } else {
      mExoPlayer.setVolume(VOLUME_NORMAL);
    }

    if (mPlayOnFocusGain) {
      mExoPlayer.setPlayWhenReady(true);
      mPlayOnFocusGain = false;
    }
  }

  private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener =
    new AudioManager.OnAudioFocusChangeListener() {
      @Override
      public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
          case AudioManager.AUDIOFOCUS_GAIN:
            mCurrentAudioFocusState = AUDIO_FOCUSED;
            break;
          case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            // Audio focus was lost, but it's possible to duck (i.e.: play quietly)
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK;
            break;
          case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            // Lost audio focus, but will gain it back (shortly), so note whether
            // playback should resume
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
            mPlayOnFocusGain = mExoPlayer != null && mExoPlayer.getPlayWhenReady();
            break;
          case AudioManager.AUDIOFOCUS_LOSS:
            // Lost audio focus, probably "permanently"
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
            break;
        }

        if (mExoPlayer != null) {
          // Update the player state based on the change
          configurePlayerState();
        }
      }
    };

  /**
   * Releases resources used by the service for playback, which is mostly just the WiFi lock for
   * local playback. If requested, the ExoPlayer instance is also released.
   *
   * @param releasePlayer Indicates whether the player should also be released
   */
  private  void releaseResources(boolean releasePlayer) {
    if (releasePlayer && mExoPlayer != null) {
      mExoPlayer.release();
      mExoPlayer.removeListener(mEventListener);
      mExoPlayer = null;
      mExoPlayerNullIsStopped = true;
      mPlayOnFocusGain = false;
    }

    if (mWifiLock.isHeld()) {
      mWifiLock.release();
    }
  }

  private void registerAudioNoisyReceiver() {
    if (mAudioNoisyReceiverRegistered) return;

    mContext.registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter);
    mAudioNoisyReceiverRegistered = true;
  }

  private void unregisterAudioNoisyReceiver() {
    if (!mAudioNoisyReceiverRegistered) return;

    mContext.unregisterReceiver(mAudioNoisyReceiver);
    mAudioNoisyReceiverRegistered = false;
  }

    private void updateProgressBar() {
        long position = mExoPlayer == null ? 0 : mExoPlayer.getCurrentPosition();

        handler.removeCallbacks(updateProgressAction);

        PodcastSessionStateManager.getInstance().saveEpisodeProgress(position);

        // Schedule an update if necessary.
        int playbackState = mExoPlayer == null ? Player.STATE_IDLE : mExoPlayer.getPlaybackState();
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            long delayMs;
            if (mExoPlayer.getPlayWhenReady() && playbackState == Player.STATE_READY) {
                delayMs = 1000 - (position % 1000);
                if (delayMs < 200) {
                    delayMs += 1000;
                }
            } else {
                delayMs = 1000;
            }
            handler.postDelayed(updateProgressAction, delayMs);
        }
    }

    private final class ExoPlayerEventListener implements ExoPlayer.EventListener {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
          // Nothing to do.
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
          // Nothing to do.
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
          switch(playbackState) {
            case ExoPlayer.STATE_IDLE:
            case ExoPlayer.STATE_BUFFERING:
            case ExoPlayer.STATE_READY:
                updateProgressBar();
                setDuration();
                if (mCallback != null)  mCallback.onPlaybackStatusChanged(getState());
                break;
            case ExoPlayer.STATE_ENDED:
                if (mCallback != null) mCallback.onCompletion();
                break;
          }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
          final String what;
          switch (error.type) {
            case ExoPlaybackException.TYPE_SOURCE:
              what = error.getSourceException().getMessage();
              break;
            case ExoPlaybackException.TYPE_RENDERER:
              what = error.getRendererException().getMessage();
              break;
            case ExoPlaybackException.TYPE_UNEXPECTED:
              what = error.getUnexpectedException().getMessage();
              break;
            default:
              what = "Unknown: " + error;
          }

          if (mCallback != null) {
            mCallback.onError("ExoPlayer error " + what);
          }
        }

        @Override
        public void onPositionDiscontinuity() {
          // Nothing to do.
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
          // Nothing to do.
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
          // Nothing to do.
        }
    }
}
