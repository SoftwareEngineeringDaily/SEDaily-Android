package com.koalatea.thehollidayinn.softwareengineeringdaily.audio;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;

/*
  Created by krh12 on 6/16/2017.
 */

/**
 * Helper class for building Media style Notifications from a
 * {@link android.support.v4.media.session.MediaSessionCompat}.
 */
public class MediaNotificationHelper extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    private MediaControllerCompat.TransportControls mTransportControls;
    private final MusicService mService;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat mController;
    private boolean mStarted = false;

    private static final String ACTION_PAUSE = "com.koalatea.thehollidayinn.softwareengineeringdaily.pause";
    private static final String ACTION_PLAY = "com.koalatea.thehollidayinn.softwareengineeringdaily.play";
    private static final String ACTION_STOP = "com.koalatea.thehollidayinn.softwareengineeringdaily.stop";
    private final PendingIntent mPauseIntent;
    private final PendingIntent mPlayIntent;
    private final PendingIntent mStopInent;

    private final NotificationManagerCompat mNotificationManager;

    public MediaNotificationHelper(MusicService service) throws RemoteException {
        mService = service;
        updateSessionToken();

        mNotificationManager = NotificationManagerCompat.from(service);

        String pkg = mService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopInent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_STOP).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);


        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();
    }

    private void updateSessionToken() throws RemoteException {
        MediaSessionCompat.Token freshToken = mService.getSessionToken();
        if (mSessionToken == null && freshToken != null ||
                mSessionToken != null && !mSessionToken.equals(freshToken)) {
            if (mController != null) {
                mController.unregisterCallback(mCb);
            }
            mSessionToken = freshToken;
            if (mSessionToken != null) {
                mController = new MediaControllerCompat(mService, mSessionToken);
                mTransportControls = mController.getTransportControls();
                if (mStarted) {
                    mController.registerCallback(mCb);
                }
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        switch (action) {
            case ACTION_PAUSE:
                mTransportControls.pause();
                break;
            case ACTION_PLAY:
                mTransportControls.play();
                break;
            case ACTION_STOP:
                mTransportControls.stop();
                break;
            default:
                Log.v("test", "Unknown intent ignored.");
        }
    }
    public Notification createNotification() {
        MediaControllerCompat controller = mController;
        MediaMetadataCompat mMetadata = controller.getMetadata();
        PlaybackStateCompat mPlaybackState = controller.getPlaybackState();

        if (mMetadata == null || mPlaybackState == null) {
            return null;
        }

        boolean isPlaying = mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING;

        NotificationCompat.Action action;
        if (isPlaying) {
            action = new NotificationCompat.Action(R.drawable.ic_pause_black_36dp,
                    mService.getString(R.string.label_pause), mPauseIntent);
        } else {
            action = new NotificationCompat.Action(R.drawable.ic_play_arrow_black_36dp,
                    mService.getString(R.string.label_play), mPlayIntent);
        }

        MediaDescriptionCompat description = mMetadata.getDescription();
        Bitmap art = description.getIconBitmap();
        if (art == null) {
            // use a placeholder art while the remote art is being downloaded.
            art = BitmapFactory.decodeResource(mService.getResources(),
                    R.drawable.sedaily_logo);
        }
        Log.v("keithtest", String.valueOf(description.getTitle()));
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService);
        notificationBuilder
                .setLargeIcon(art)
                .setContentIntent(controller.getSessionActivity())
                .setDeleteIntent(mStopInent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.sedaily_logo)
                .setColor(ContextCompat.getColor(mService, R.color.colorPrimaryDark))
                .addAction(action)
                .setStyle(new NotificationCompat.MediaStyle()
                        // show only play/pause in compact view.
                        .setShowActionsInCompactView(new int[]{0})
                        .setMediaSession(mSessionToken))
                .setShowWhen(false)
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle());


        return notificationBuilder.build();
    }

    public void startNotification() {
        if (!mStarted) {
            // The notification must be updated after setting started to true
            Notification notification = createNotification();
            if (notification != null) {
                mController.registerCallback(mCb);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_STOP);
                mService.registerReceiver(this, filter);

                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        }
    }

    private void stopNotification() {
        if (mStarted) {
            mStarted = false;
            mController.unregisterCallback(mCb);
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
        }
    }

    private final MediaControllerCompat.Callback mCb = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            if (state.getState() == PlaybackStateCompat.STATE_STOPPED ||
                    state.getState() == PlaybackStateCompat.STATE_NONE) {
                stopNotification();
            } else {
                Notification notification = createNotification();
                if (notification != null) {
                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Notification notification = createNotification();
            if (notification != null) {
                mNotificationManager.notify(NOTIFICATION_ID, notification);
            }
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            try {
                updateSessionToken();
            } catch (RemoteException e) {
//                LogHelper.e(TAG, e, "could not connect media controller");
            }
        }
    };

}