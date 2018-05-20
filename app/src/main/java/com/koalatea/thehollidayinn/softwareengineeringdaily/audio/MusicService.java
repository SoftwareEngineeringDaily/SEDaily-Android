package com.koalatea.thehollidayinn.softwareengineeringdaily.audio;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;


import com.koalatea.thehollidayinn.softwareengineeringdaily.MainActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MusicProvider.MEDIA_ID_EMPTY_ROOT;

public class MusicService extends MediaBrowserServiceCompat implements
        PlaybackManager.PlaybackServiceCallback {
    // The action of the incoming Intent indicating that it contains a command
    // to be executed (see {@link #onStartCommand})
    public static final String ACTION_CMD = "com.koalatea.thehollidayinn.ACTION_CMD";
    // The key in the extras of the incoming Intent indicating the command that
    // should be executed (see {@link #onStartCommand})
    public static final String CMD_NAME = "CMD_NAME";
    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be paused (see {@link #onStartCommand})
    public static final String CMD_PAUSE = "CMD_PAUSE";

    // Request code for starting the UI.
    private static final int REQUEST_CODE = 99;

    // Delay stopSelf by using a handler.
    private static final long STOP_DELAY = TimeUnit.SECONDS.toMillis(30);

    private MusicProvider mMusicProvider;
    private PlaybackManager mPlaybackManager;

    private MediaSessionCompat mSession;
    private MediaNotificationManager mMediaNotificationManager;
    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);

    @Override
    public void onCreate() {
        super.onCreate();

        loadMusicProvider();
        createPlaybackManager();
        createMediaSession();
        setUpSessionPendingIntent();
        createNotificationManager();

        // @TODO: https://github.com/googlesamples/android-UniversalMusicPlayer/blob/master/mobile/src/main/java/com/example/android/uamp/MusicService.java#L230
    }

    private void createPlaybackManager() {
        QueueManager queueManager = loadQueueManager();
        // @TODO: Maybe package Validator
        LocalPlayback mLocalPlayback = new LocalPlayback(this, mMusicProvider);
        mPlaybackManager = new PlaybackManager(this, getResources(), mMusicProvider, queueManager, mLocalPlayback);
    }

    private void createMediaSession() {
        mSession = new MediaSessionCompat(this, "MusicService");
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(mPlaybackManager.getMediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }

    private void setUpSessionPendingIntent() {
        Context context = getApplicationContext();
        // This is an Intent to launch the app's UI, used primarily by the ongoing notification.
        Intent intent = new Intent(context, MainActivity.class); // @TOOD: Now playing?
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);

        Bundle mSessionExtras = new Bundle();
        mSession.setExtras(mSessionExtras);

        // @TODO: Do we need to do this here?
        mPlaybackManager.updatePlaybackState(null);
    }

    private void createNotificationManager() {
        try {
            mMediaNotificationManager = new MediaNotificationManager(this);
        } catch (RemoteException e) {
            // @TODO Had a report of no method printStackTrace and this was the only call... not sure why
//            e.printStackTrace();
        }
    }

    private void loadMusicProvider() {
        mMusicProvider = MusicProvider.getInstance();
        // To make the app more responsive, fetch and cache catalog information now.
        // This can help improve the response time in the method
        // {@link #onLoadChildren(String, Result<List<MediaItem>>) onLoadChildren()}.
        mMusicProvider.retrieveMediaAsync(null /* Callback */);
    }

    private QueueManager loadQueueManager() {
        QueueManager queueManager = new QueueManager(mMusicProvider,
            new QueueManager.MetadataUpdateListener() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    mSession.setMetadata(metadata);
                }

                @Override
                public void onMetadataRetrieveError() {
                    mPlaybackManager.updatePlaybackState(
                            getString(R.string.error_no_metadata));
                }

                @Override
                public void onCurrentQueueIndexUpdated(int queueIndex) {
//                    mPlaybackManager.handlePlayRequest();
                }

                @Override
                public void onQueueUpdated(String title,
                                           List<MediaSessionCompat.QueueItem> newQueue) {
                    mSession.setQueue(newQueue);
                    mSession.setQueueTitle(title);
                }
            });

        return queueManager;
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            handleStartIntent(startIntent);
        }

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        return START_STICKY;
    }

    private void handleStartIntent(Intent startIntent) {
        String action = startIntent.getAction();
        String command = startIntent.getStringExtra(CMD_NAME);

        if (!ACTION_CMD.equals(action)) {
            MediaButtonReceiver.handleIntent(mSession, startIntent);
            return;
        }

        if (CMD_PAUSE.equals(command)) {
            mPlaybackManager.handlePauseRequest();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        // @TODO: unregisterCarConnectionReceiver();

        // Service is being killed, so make sure we release our resources
        mPlaybackManager.handleStopRequest(null);
        mMediaNotificationManager.stopNotification();

        // @TODO: https://github.com/googlesamples/android-UniversalMusicPlayer/blob/master/mobile/src/main/java/com/example/android/uamp/MusicService.java#L294

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        // Always release the MediaSession to clean up resources
        // and notify associated MediaController(s).
        mSession.release();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName,
                                 int clientUid, Bundle rootHints) {
        // @TODO: package validator
        // Verify the client is authorized to browse media and return the root that
        // makes the most sense here. In this example we simply verify the package name
        // is the same as ours, but more complicated checks, and responses, are possible
        if (!clientPackageName.equals(getPackageName())) {
            // Allow the client to connect, but not browse, by returning an empty root
            return new MediaBrowserServiceCompat.BrowserRoot(MEDIA_ID_EMPTY_ROOT, null);
        }

        return new BrowserRoot(MusicProvider.MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {

        if (MEDIA_ID_EMPTY_ROOT.equals(parentMediaId)) {
            result.sendResult(new ArrayList<MediaBrowserCompat.MediaItem>());
        } else if (mMusicProvider.isInitialized()) {
//            result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
        } else {
            result.detach();
//            mMusicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
//                @Override
//                public void onMusicCatalogReady(boolean success) {
//                    result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
//                }
//            });
        }
    }

    @Override
    public void onPlaybackStart() {
        mSession.setActive(true);
        mDelayedStopHandler.removeCallbacksAndMessages(null);

        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(new Intent(getApplicationContext(), MusicService.class));
    }

    @Override
    public void onPlaybackStop() {
        mSession.setActive(false);
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        stopForeground(true);
    }

    @Override
    public void onNotificationRequired() {
        mMediaNotificationManager.startNotification();
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mSession.setPlaybackState(newState);
    }

    // @TODO: Car connections

    // @TODO: WHERE??
//    private void handleSpeedChange(int speed) {
//        mLocalPlayback.setSpeed(speed);
//    }

    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();
            if (service != null && service.mPlaybackManager.getPlayback() != null) {
                if (service.mPlaybackManager.getPlayback().isPlaying()) {
                    return;
                }
                service.stopSelf();
            }
        }
    }
}
