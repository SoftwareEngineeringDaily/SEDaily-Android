package com.koalatea.thehollidayinn.softwareengineeringdaily.mediaui;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MusicProvider;
import com.koalatea.thehollidayinn.softwareengineeringdaily.podcast.PodcastSessionStateManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/*
 * Created by keithholliday on 9/26/17.
 */

public class PlaybackControlsFragment extends Fragment {
  @BindView(R.id.play_pause)
  ImageButton playPause;

  @BindView(R.id.title)
  TextView title;

  @BindView(R.id.seekBar1)
  SeekBar mSeekbar;

  @BindView(R.id.startText)
  TextView mStart;

  @BindView(R.id.endText)
  TextView mEnd;

  @BindView(R.id.speed) Button speed;

  private PlaybackStateCompat mLastPlaybackState;
  private static final long PROGRESS_UPDATE_INTERNAL = 1000;
  private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
  private final Handler mHandler = new Handler();
  private ScheduledFuture<?> mScheduleFuture;
  private final ScheduledExecutorService mExecutorService =
          Executors.newSingleThreadScheduledExecutor();
  private final Runnable mUpdateProgressTask = new Runnable() {
      @Override
      public void run() {
          updateProgress();
      }
  };
  private DisposableObserver speedSubscription;

  // Receive callbacks from the MediaController. Here we update our state such as which queue
  // is being shown, the current title and description and the PlaybackState.
  // From: https://github.com/googlesamples/android-UniversalMusicPlayer/blob/39fa286313639b5ce069e755c18762aaa1f59ea9/mobile/src/main/java/com/example/android/uamp/ui/PlaybackControlsFragment.java
  private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
      @Override
      public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
          handlePlaybackStateChange(state);
          mLastPlaybackState = state;

          switch (state.getState()) {
              case PlaybackStateCompat.STATE_PLAYING:
                  scheduleSeekbarUpdate();
                  break;
              case PlaybackStateCompat.STATE_PAUSED:
                  stopSeekbarUpdate();
                  break;
              case PlaybackStateCompat.STATE_STOPPED:
                  stopSeekbarUpdate();
                  break;
          }
      }

      @Override
      public void onMetadataChanged(MediaMetadataCompat metadata) {
          if (metadata == null) {
              return;
          }
          updateWithMeta(metadata);
      }
  };

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false);

    ButterKnife.bind(this, rootView);

    playPause.setEnabled(true);
    playPause.setOnClickListener(buttonListener);

    mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          mStart.setText(DateUtils.formatElapsedTime(progress / 1000));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            stopSeekbarUpdate();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
          MediaControllerCompat.getMediaController(PlaybackControlsFragment.this.getActivity()).getTransportControls().seekTo(seekBar.getProgress());
          scheduleSeekbarUpdate();
        }
    });

    speed.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        showSpeedDialog();
      }
    });

    setSpeedText();
    setUpSpeedSubscription();

    return rootView;
  }

  @Override
  public void onStart() {
    super.onStart();
    long currentPlayTime = PodcastSessionStateManager.getInstance().getCurrentProgress();
    mLastPlaybackState = PodcastSessionStateManager.getInstance().getLastPlaybackState();

    if (currentPlayTime > 0) {
        scheduleSeekbarUpdate();
    }
    mStart.setText(DateUtils.formatElapsedTime(currentPlayTime / 1000));
    setSpeedTextView();

    if (speedSubscription != null && speedSubscription.isDisposed()) {
      setUpSpeedSubscription();
    }
  }

  private void setUpSpeedSubscription () {
    speedSubscription = new DisposableObserver<Integer>() {
        @Override public void onError(Throwable e) {
        }

        @Override
        public void onComplete() {

        }

        @Override public void onNext(Integer integer) {
        setSpeedText();
      }
    };
    PodcastSessionStateManager
        .getInstance()
        .getSpeedChanges()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(speedSubscription);
  }

  @Override
  public void onStop() {
      super.onStop();
      MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
      if (controller != null) {
          controller.unregisterCallback(mCallback);
      }

      if (speedSubscription != null) {
        speedSubscription.dispose();
      }

      stopSeekbarUpdate();
  }

  @Override
  public void onPause() {
      super.onPause();
      if (mLastPlaybackState == null) {
          return;
      }

      long currentPosition = mLastPlaybackState.getPosition();
      if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
          // Calculate the elapsed time between the last position update and now and unless
          // paused, we can assume (delta * speed) + current position is approximately the
          // latest position. This ensure that we do not repeatedly call the getPlaybackState()
          // on MediaControllerCompat.
          long timeDelta = SystemClock.elapsedRealtime() -
                  mLastPlaybackState.getLastPositionUpdateTime();
          currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
      }

      PodcastSessionStateManager.getInstance().setCurrentProgress(currentPosition);
      PodcastSessionStateManager.getInstance().setLastPlaybackState(mLastPlaybackState);
      stopSeekbarUpdate();
  }

  @Override
  public void onDestroy() {
      super.onDestroy();
      stopSeekbarUpdate();
  }

  public void onConnected() {
    MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
    if (controller != null) {
      updateWithMeta(controller.getMetadata());
      handlePlaybackStateChange(controller.getPlaybackState());
      controller.registerCallback(mCallback);
    }
  }

  private void updateWithMeta(MediaMetadataCompat metadata) {
      if (getActivity() == null) {
          // "onMetadataChanged called when getActivity null," +
          // "this should not happen if the callback was properly unregistered. Ignoring.");
          return;
      }

      if (metadata == null) {
          return;
      }

      updateDuration(metadata);

      // @TODO: Verify that we need this
      MediaMetadataCompat latest = MusicProvider.getInstance().getMusic(metadata.getDescription().getMediaId());

      title.setText(latest.getDescription().getTitle());
  }

  private void handlePlaybackStateChange(PlaybackStateCompat state) {
      if (getActivity() == null) {
          return;
      }

      if (state == null) {
          return;
      }

      boolean enablePlay = false;

      switch (state.getState()) {
          case PlaybackStateCompat.STATE_PAUSED:
          case PlaybackStateCompat.STATE_STOPPED:
              enablePlay = true;
              break;
          case PlaybackStateCompat.STATE_ERROR:
              // @TODO: Log error
              break;
      }

      if (enablePlay) {
          playPause.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_play_arrow_black_36dp));
      } else {
          playPause.setImageDrawable(
                  ContextCompat.getDrawable(getActivity(), R.drawable.ic_pause_black_36dp));
      }
  }

  private final View.OnClickListener buttonListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
    MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
    if (controller == null) return;

    PlaybackStateCompat stateObj = controller.getPlaybackState();
    final int state = stateObj == null ? PlaybackStateCompat.STATE_NONE : stateObj.getState();

    switch (v.getId()) {
      case R.id.play_pause:
        // @TODO: Refactor these to in array
        if (state == PlaybackStateCompat.STATE_PAUSED ||
                state == PlaybackStateCompat.STATE_STOPPED ||
                state == PlaybackStateCompat.STATE_NONE) {
          playMedia();
        } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_CONNECTING) {
          pauseMedia();
        }
          break;
    }
      }
  };

  private void playMedia() {
      MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
      if (controller != null) {
          controller.getTransportControls().play();
      }
  }

  private void pauseMedia() {
      MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
      if (controller != null) {
          controller.getTransportControls().pause();
      }
  }

  private void updateDuration(MediaMetadataCompat metadata) {
      if (metadata == null) {
          return;
      }

      MediaMetadataCompat latest = MusicProvider.getInstance().getMusic(metadata.getDescription().getMediaId());

      if (latest == null) {
          return;
      }

      int duration = (int) latest.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
      mSeekbar.setMax(duration);
      mEnd.setText(DateUtils.formatElapsedTime(duration/1000));
  }

  private void scheduleSeekbarUpdate() {
      stopSeekbarUpdate();
      if (!mExecutorService.isShutdown()) {
          mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                  new Runnable() {
                      @Override
                      public void run() {
                      mHandler.post(mUpdateProgressTask);
                      }
                  }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                  PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
      }
  }

  private void stopSeekbarUpdate() {
      if (mScheduleFuture != null) {
          mScheduleFuture.cancel(false);
      }
  }

  private void updateProgress() {
      if (mLastPlaybackState == null) {
          return;
      }
      long currentPosition = mLastPlaybackState.getPosition();
      if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
          // Calculate the elapsed time between the last position update and now and unless
          // paused, we can assume (delta * speed) + current position is approximately the
          // latest position. This ensure that we do not repeatedly call the getPlaybackState()
          // on MediaControllerCompat.
          long timeDelta = SystemClock.elapsedRealtime() -
                  mLastPlaybackState.getLastPositionUpdateTime();
          currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
      }
      mSeekbar.setProgress((int) currentPosition);
  }

  private void showSpeedDialog() {
    new SpeedDialog().show(this.getFragmentManager(), "tag");
  }

  private int setSpeedTextView () {
    int currentSpeed = PodcastSessionStateManager.getInstance().getCurrentSpeed();
    String[] speedArray = getResources().getStringArray(R.array.speed_options);
    speed.setText(speedArray[currentSpeed]);
    return currentSpeed;

  }
  private void setSpeedText() {
    int currentSpeed = setSpeedTextView();

    MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
    if (controller != null) {
      Bundle args = new Bundle();
      args.putInt("SPEED", currentSpeed);
      // @TODO: Make constant
      controller.getTransportControls().sendCustomAction("SPEED_CHANGE", args);
    }
  }
}
