package com.koalatea.thehollidayinn.softwareengineeringdaily;

import android.content.ComponentName;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MusicService;
import com.koalatea.thehollidayinn.softwareengineeringdaily.mediaui.PlaybackControlsFragment;

/*
 * Created by keithholliday on 9/27/17.
 */

// @TODO: Some of this needs to be moved to the fragment
// @TODO: Abstract the business logic for Clean Architecture

public class PlaybackControllerActivity extends AppCompatActivity {
    private static final String TAG = "PlaybackController";
    private MediaBrowserCompat mMediaBrowser;
    private PlaybackControlsFragment mControlsFragment;
    private String mCurrentMediaId = "";

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        Log.v(TAG, e.toString());
                    }
                }

                @Override
                public void onConnectionSuspended() {
                    // The Service has crashed. Disable transport controls until it automatically reconnects
                }

                @Override
                public void onConnectionFailed() {
                    // The Service has refused our connection
                }
            };

    private final MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        hidePlaybackControls();
                    }
                }
            };

    protected void setUp() {
        if (mMediaBrowser == null) {
            mMediaBrowser = new MediaBrowserCompat(this,
                    new ComponentName(this, MusicService.class),
                    mConnectionCallbacks,
                    null); // optional Bundle
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mControlsFragment == null) {
            mControlsFragment = (PlaybackControlsFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_playback_controls);
        }
        mMediaBrowser.connect();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(controllerCallback);
        }
        mMediaBrowser.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        // (see "stay in sync with the MediaSession")
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(controllerCallback);
        }
        mMediaBrowser.disconnect();
    }

    private void showPlaybackControls() {
        // @TODO: check for network
        getSupportFragmentManager().beginTransaction()
                .show(mControlsFragment)
                .commitAllowingStateLoss();

    }

    private void hidePlaybackControls() {
        getSupportFragmentManager().beginTransaction()
                .hide(mControlsFragment)
                .commitAllowingStateLoss();
    }

    /**
     * Check if the MediaSession is active and in a "playback-able" state
     * (not NONE and not STOPPED).
     *
     * @return true if the MediaSession's state requires playback controls to be visible.
     */
    private boolean shouldShowControls() {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(this);
        if (mediaController == null ||
                mediaController.getMetadata() == null ||
                mediaController.getPlaybackState() == null) {
            return false;
        }
        switch (mediaController.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_ERROR:
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                return false;
            default:
                return true;
        }
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(
                this, token);

        MediaControllerCompat.setMediaController(this, mediaController);
        mediaController.registerCallback(controllerCallback);

        if (shouldShowControls()) {
            showPlaybackControls();
        } else {
            hidePlaybackControls();
        }

        if (mControlsFragment != null) {
            mControlsFragment.onConnected();
        }
    }

    protected void onMediaItemSelected(MediaBrowserCompat.MediaItem item, boolean isSameMedia) {
        if (item.isPlayable()) {
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
            MediaControllerCompat.TransportControls controls = controller.getTransportControls();

            if (isSameMedia) {
                if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    controls.pause();
                } else if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
                    controls.play();
                }
            } else {
                controls.playFromMediaId(item.getMediaId(), null);
                mCurrentMediaId = item.getMediaId();
            }
        }
    }

    @Nullable protected String getPlayingMediaId() {
      // @TODO this class need to watch set and media
        //boolean isPlaying = state != null
        //        && state.getState() == PlaybackStateCompat.STATE_PLAYING;
        //return isPlaying ? mCurrentMediaId : null;
        return mCurrentMediaId;
    }
}
