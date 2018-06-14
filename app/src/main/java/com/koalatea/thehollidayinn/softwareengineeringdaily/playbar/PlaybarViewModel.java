package com.koalatea.thehollidayinn.softwareengineeringdaily.playbar;

import android.app.Activity;
import android.arch.lifecycle.ViewModel;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.koalatea.thehollidayinn.softwareengineeringdaily.podcast.PodcastSessionStateManager;

/**
 * Created by keithholliday on 2/6/18.
 */

public class PlaybarViewModel extends ViewModel {
    public void sendSpeedChangeIntent(int currentSpeed, Activity activity) {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(activity);
        if (controller != null) {
            Bundle args = new Bundle();
            args.putInt("SPEED", currentSpeed);
            // @TODO: Make constant
            controller.getTransportControls().sendCustomAction("SPEED_CHANGE", args);
        }
    }

    public long setListenedProgress(PlaybackStateCompat mLastPlaybackState) {
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

        // Save progress for episode
        String postTile = PodcastSessionStateManager.getInstance().getCurrentTitle();
        if (!postTile.isEmpty()) {
            PodcastSessionStateManager.getInstance().setProgressForEpisode(postTile, currentPosition);
        }

        return currentPosition;
    }

    public void playPause(MediaControllerCompat controller) {
        PlaybackStateCompat stateObj = controller.getPlaybackState();
        final int state = stateObj == null ? PlaybackStateCompat.STATE_NONE : stateObj.getState();

        if (state == PlaybackStateCompat.STATE_PAUSED ||
                state == PlaybackStateCompat.STATE_STOPPED ||
                state == PlaybackStateCompat.STATE_NONE) {
            playMedia(controller);
        } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_CONNECTING) {
            pauseMedia(controller);
        }
    }

    private void playMedia(MediaControllerCompat controller) {
        if (controller != null) {
            controller.getTransportControls().play();
        }
    }

    private void pauseMedia(MediaControllerCompat controller) {
        if (controller != null) {
            controller.getTransportControls().pause();
        }
    }

    public void back15(Activity activity) {
        movePlayback(activity, "MOVE_BACK");
    }

    public void skip15(Activity activity) {
        movePlayback(activity, "MOVE_FORWARD");
    }

    private void movePlayback(Activity activity, String action) {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(activity);
        if (controller == null) return;

        Bundle args = new Bundle();
        args.putInt("DISTANCE", 15000);
        // @TODO: Make constant
        controller.getTransportControls().sendCustomAction(action, args);

    }
}
