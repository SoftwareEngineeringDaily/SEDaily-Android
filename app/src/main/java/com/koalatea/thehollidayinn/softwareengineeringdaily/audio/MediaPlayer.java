package com.koalatea.thehollidayinn.softwareengineeringdaily.audio;

import android.app.Activity;
import android.content.ComponentName;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.koalatea.thehollidayinn.softwareengineeringdaily.adapters.PodcastAdapter;

import java.util.List;

/**
 * Created by krh12 on 6/23/2017.
 */

public class MediaPlayer {
    private static MediaPlayer instance = null;
    private Activity activity;
    private PlaybackStateCompat state;
    private MediaMetadataCompat metadata;
    private String mCurrentMediaId;
    public static final String ARG_MEDIA_ID = "media_id";
    private static final String TAG = "newkeithtest";
    private String mMediaId;
    private MediaBrowserCompat mMediaBrowser;

    private MediaPlayer(Activity activity) {
        this.activity = activity;
        mMediaBrowser = new MediaBrowserCompat(activity,
                new ComponentName(activity, MusicService.class),
                mConnectionCallback, null);

        mMediaBrowser.connect();
    }

    public static MediaPlayer getInstance(Activity activity) {
        if(instance == null) {
            instance = new MediaPlayer(activity);
        }
        return instance;
    }

    public void onMediaItemSelected(MediaBrowserCompat.MediaItem item, boolean isPlaying) {
        if (item.isPlayable()) {
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(activity);
            MediaControllerCompat.TransportControls controls = controller.getTransportControls();

            if (isPlaying) {
                controls.pause();
            } else {
                controls.playFromMediaId(item.getMediaId(), null);
            }
        }
    }

    private MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {

                @Override
                public void onChildrenLoaded(String parentId,
                                             List<MediaBrowserCompat.MediaItem> children) {
//                    podcastAdapter.notifyDataSetChanged();
//                    for (MediaBrowserCompat.MediaItem item : children) {
//                        podcastAdapter.add(item);
//                    }
//                    podcastAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(String id) {
//                    Toast.makeText(getActivity(), R.string.error_loading_media,
//                            Toast.LENGTH_LONG).show();
                }
            };

    private MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "onConnected: session token " + mMediaBrowser.getSessionToken());

                    if (mMediaId == null) {
                        mMediaId = mMediaBrowser.getRoot();
                    }
                    mMediaBrowser.subscribe(mMediaId, mSubscriptionCallback);
                    try {
                        MediaControllerCompat mediaController =
                                new MediaControllerCompat(activity,
                                        mMediaBrowser.getSessionToken());
                        MediaControllerCompat.setMediaController(activity, mediaController);

                        // Register a Callback to stay in sync
                        mediaController.registerCallback(mControllerCallback);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Failed to connect to MediaController", e);
                    }
                }

                @Override
                public void onConnectionFailed() {
                    Log.e(TAG, "onConnectionFailed");
                }

                @Override
                public void onConnectionSuspended() {
                    Log.d(TAG, "onConnectionSuspended");
                    MediaControllerCompat mediaController = MediaControllerCompat
                            .getMediaController(activity);
                    if (mediaController != null) {
                        mediaController.unregisterCallback(mControllerCallback);
                        MediaControllerCompat.setMediaController(activity, null);
                    }
                }
            };

    private MediaControllerCompat.Callback mControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat newMetadata) {
                    if (newMetadata != null) {
//                        podcastAdapter.setCurrentMediaMetadata(metadata);
                        setCurrentMediaMetadata(newMetadata);
                        metadata = newMetadata;
                    }
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat newState) {
//                    podcastAdapter.setPlaybackState(state);
//                    podcastAdapter.notifyDataSetChanged();
//                    state = newState;
                    setPlaybackState(newState);
                }
            };

    @Nullable
    public String getPlayingMediaId() {
        boolean isPlaying = state != null
                && state.getState() == PlaybackStateCompat.STATE_PLAYING;
        return isPlaying ? mCurrentMediaId : null;
    }

    public void setCurrentMediaMetadata(MediaMetadataCompat mediaMetadata) {
        if (mediaMetadata == null) {
            return;
        }
        mCurrentMediaId = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
    }

    public void setPlaybackState(PlaybackStateCompat playbackState) {
        state = playbackState;
    }
}
