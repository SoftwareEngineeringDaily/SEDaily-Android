package com.koalatea.thehollidayinn.softwareengineeringdaily.audio;

/**
 * Created by krh12 on 6/16/2017.
 */

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;

/**
 * Helper class for building Media style Notifications from a
 * {@link android.support.v4.media.session.MediaSessionCompat}.
 */
public class MediaNotificationHelper {
    private MediaNotificationHelper() {
        // Helper utility class; do not instantiate.
    }

    public static Notification createNotification(Context context,
                                                  MediaSessionCompat mediaSession) {
        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mMetadata = controller.getMetadata();
        PlaybackStateCompat mPlaybackState = controller.getPlaybackState();

        if (mMetadata == null || mPlaybackState == null) {
            return null;
        }

        boolean isPlaying = mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING;
        NotificationCompat.Action action = isPlaying
                ? new NotificationCompat.Action(R.drawable.ic_up_arrow,
                context.getString(R.string.label_pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_PAUSE))
                : new NotificationCompat.Action(R.drawable.ic_down_arrow,
                context.getString(R.string.label_play),
                MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_PLAY));

        MediaDescriptionCompat description = mMetadata.getDescription();
        Bitmap art = description.getIconBitmap();
        if (art == null) {
            // use a placeholder art while the remote art is being downloaded.
            art = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.sedaily_logo);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder
                .setStyle(new NotificationCompat.MediaStyle()
                        // show only play/pause in compact view.
                        .setShowActionsInCompactView(new int[]{0})
                        .setMediaSession(mediaSession.getSessionToken()))
                .addAction(action)
                .setSmallIcon(R.drawable.sedaily_logo)
                .setShowWhen(false)
                .setContentIntent(controller.getSessionActivity())
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setLargeIcon(art)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        return notificationBuilder.build();
    }
}