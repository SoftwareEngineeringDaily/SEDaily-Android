package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.support.v4.media.session.PlaybackStateCompat;

/*
 * Created by keithholliday on 9/30/17.
 */

// @TODO: use Android architecture to maintain state instead
public class PodcastSessionStateManager {
    private static PodcastSessionStateManager instance = null;

    private long currentProgress = 0;

    private PlaybackStateCompat lastPlaybackState;

    private PodcastSessionStateManager() {
    }

    public static PodcastSessionStateManager getInstance() {
        if(instance == null) {
            instance = new PodcastSessionStateManager();
        }
        return instance;
    }

    public long getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(long currentProgress) {
        this.currentProgress = currentProgress;
    }

    public PlaybackStateCompat getLastPlaybackState() {
        return lastPlaybackState;
    }

    public void setLastPlaybackState(PlaybackStateCompat lastPlaybackState) {
        this.lastPlaybackState = lastPlaybackState;
    }
}
