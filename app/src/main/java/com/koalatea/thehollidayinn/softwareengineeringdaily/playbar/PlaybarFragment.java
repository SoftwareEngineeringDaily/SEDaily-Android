package com.koalatea.thehollidayinn.softwareengineeringdaily.playbar;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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
import com.koalatea.thehollidayinn.softwareengineeringdaily.mediaui.SpeedDialog;
import com.koalatea.thehollidayinn.softwareengineeringdaily.podcast.PodcastSessionStateManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/*
 * Created by keithholliday on 9/26/17.
 */

public class PlaybarFragment extends Fragment {
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

    private PlaybarViewModel playbarViewModel;

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
    private DisposableObserver mediaItemSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                   Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false);

      ButterKnife.bind(this, rootView);

      playbarViewModel = ViewModelProviders
            .of(this)
            .get(PlaybarViewModel.class);

      playPause.setEnabled(true);

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
          Activity activity = PlaybarFragment.this.getActivity(); // @TODO: can we use app context?
          if (activity == null) {
              return;
          }
          MediaControllerCompat.getMediaController(activity)
            .getTransportControls()
            .seekTo(seekBar.getProgress());
          scheduleSeekbarUpdate();
        }
      });

      setSpeedText();
      setUpSpeedSubscription();
      setUpMediaChangeSubscription();

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

        if (mediaItemSubscription != null && mediaItemSubscription.isDisposed()) {
          setUpMediaChangeSubscription();
        }
    }

    @Override
    public void onStop() {
      super.onStop();

      if (speedSubscription != null) {
        speedSubscription.dispose();
      }

      if (mediaItemSubscription != null) {
        mediaItemSubscription.dispose();
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

    @OnClick(R.id.play_pause)
    public void handlePlayClick () {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller == null) return;
        playbarViewModel.playPause(controller);
    }

    /* Playback events */

    public void onConnected() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
          updateWithMeta(controller.getMetadata());
          handlePlaybackStateChange(controller.getPlaybackState());
        }
    }

    public void updateWithMeta(MediaMetadataCompat metadata) {
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
      MusicProvider musicProvider = MusicProvider.getInstance();
      MediaMetadataCompat latest = musicProvider.getMusic(metadata.getDescription().getMediaId());

      title.setText(latest.getDescription().getTitle());

      String postTile = latest.getDescription().getTitle().toString();
      MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());

      PodcastSessionStateManager psm = PodcastSessionStateManager.getInstance();
      String activeTitle = psm.getCurrentTitle();
      if (controller != null && !postTile.isEmpty() && !postTile.equals(activeTitle)) {
        psm.setCurrentTitle(postTile);
        long currentPlayPosition = psm.getProgressForEpisode(postTile);
        controller.getTransportControls().seekTo(currentPlayPosition);
        mStart.setText(DateUtils.formatElapsedTime(currentPlayPosition / 1000));
      }
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

    /* Seek bar/Progress */
    private void updateDuration(MediaMetadataCompat metadata) {
      if (metadata == null) {
          return;
      }

      String mediaId = metadata.getDescription().getMediaId();
      MusicProvider provider = MusicProvider.getInstance();
      MediaMetadataCompat latest = provider.getMusic(mediaId);
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
          },
          PROGRESS_UPDATE_INITIAL_INTERVAL,
          PROGRESS_UPDATE_INTERNAL,
          TimeUnit.MILLISECONDS
        );
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

      // @TODO: Make reactive
      long currentPosition = playbarViewModel.setListenedProgress(mLastPlaybackState);
      mSeekbar.setProgress((int) currentPosition);
    }

    /* Speed */

    @OnClick(R.id.speed)
    public void showSpeedDialog() {
        new SpeedDialog().show(this.getFragmentManager(), "tag");
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

    private void setUpMediaChangeSubscription () {
      mediaItemSubscription = new DisposableObserver<MediaMetadataCompat>() {
        @Override
        public void onNext(MediaMetadataCompat mediaMetadataCompat) {
          updateDuration(mediaMetadataCompat);
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onComplete() {

        }
      };

      PodcastSessionStateManager
              .getInstance()
              .getMetadataChanges()
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(mediaItemSubscription);
    }

    private int setSpeedTextView () {
        int currentSpeed = PodcastSessionStateManager.getInstance().getCurrentSpeed();
        String[] speedArray = getResources().getStringArray(R.array.speed_options);
        speed.setText(speedArray[currentSpeed]);
        return currentSpeed;
    }

    private void setSpeedText() {
        int currentSpeed = setSpeedTextView();

        // @TODO: Make reactive
        playbarViewModel.sendSpeedChangeIntent(currentSpeed, getActivity());
    }

    public void handlePlaybackState(PlaybackStateCompat state) {
      if (state == null) return;

      handlePlaybackStateChange(state);
      mLastPlaybackState = state;

      PodcastSessionStateManager.getInstance().setLastPlaybackState(mLastPlaybackState);

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

    @OnClick(R.id.back15)
    public void back15() {
      playbarViewModel.back15(this.getActivity());
    }

    @OnClick(R.id.skip15)
    public void skip15() {
      playbarViewModel.skip15(this.getActivity());
    }
}
