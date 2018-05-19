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

import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MusicService;
import com.koalatea.thehollidayinn.softwareengineeringdaily.playbar.PlaybarFragment;

/*
 * Created by keithholliday on 9/27/17.
 */

// @TODO: Some of this needs to be moved to the fragment

public class PlaybackControllerActivity extends AppCompatActivity {
    private MediaBrowserCompat mMediaBrowser;
    private PlaybarFragment mControlsFragment;

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        hidePlaybackControls(null);
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
                    updateWithMeta(metadata);
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    if (shouldShowControls()) {
                        showPlaybackControls(state);
                        return;
                    }

                    hidePlaybackControls(state);
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
            mControlsFragment = (PlaybarFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_playback_controls);
        }

        // Handle showing here?

        try {
            mMediaBrowser.connect();
        } catch (RuntimeException e) {
            // @TODO: log?
        }
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

        if (mMediaBrowser == null) return;

        mMediaBrowser.disconnect();
    }

    private void showPlaybackControls(PlaybackStateCompat state) {
        // @TODO: check for network
        getSupportFragmentManager().beginTransaction()
//                .setCustomAnimations(
//                        R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom,
//                        R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom)
                .show(mControlsFragment)
                .commitAllowingStateLoss();

        mControlsFragment.handlePlaybackState(state);
    }

    private void hidePlaybackControls(PlaybackStateCompat state) {
        getSupportFragmentManager().beginTransaction()
                .hide(mControlsFragment)
                .commitAllowingStateLoss();

        mControlsFragment.handlePlaybackState(state);
    }

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
            showPlaybackControls(null);
        } else {
            hidePlaybackControls(null);
        }

        if (mControlsFragment != null) {
            mControlsFragment.onConnected();
        }

//        @TODO: onMediaControllerConnected();
    }

    protected void onMediaItemSelected(MediaBrowserCompat.MediaItem item, boolean isSameMedia) {
        if (!item.isPlayable()) return;

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
        if (controller == null) return;

        MediaControllerCompat.TransportControls controls = controller.getTransportControls();

        if (!isSameMedia) {
            controls.playFromMediaId(item.getMediaId(), null);
            return;
        }

        int state = controller.getPlaybackState().getState();

        if (state == PlaybackStateCompat.STATE_PLAYING) {
            controls.pause();
            return;
        }

        controls.play();
    }

    private void updateWithMeta(MediaMetadataCompat metadata) {
        if (mControlsFragment == null) return;

        // @TODO: Look back at the example form Google. We know too much about the fragment I think
        mControlsFragment.updateWithMeta(metadata);
    }
}
