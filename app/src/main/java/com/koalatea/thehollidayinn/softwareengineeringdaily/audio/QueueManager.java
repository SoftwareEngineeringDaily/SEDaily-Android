package com.koalatea.thehollidayinn.softwareengineeringdaily.audio;

import android.content.res.Resources;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * Created by keithholliday on 4/29/18.
 */

public class QueueManager {
  private MusicProvider mMusicProvider;
  private MetadataUpdateListener mListener;

  private List<MediaSessionCompat.QueueItem> mPlayingQueue;
  private int mCurrentIndex;

  public QueueManager(@NonNull MusicProvider musicProvider,
                      @NonNull MetadataUpdateListener listener) {
    this.mMusicProvider = musicProvider;
    this.mListener = listener;

    mPlayingQueue = Collections.synchronizedList(new ArrayList<MediaSessionCompat.QueueItem>());
    mCurrentIndex = 0;
  }

  public boolean isSameBrowingCategory(@NonNull String mediaId) {
    String[] newBrowserHeirarchy = MediaIDHelper.getHierarchy(mediaId);

    MediaSessionCompat.QueueItem current = getCurrentMusic();
    if (current == null) {
      return false;
    }

    String[] currentBrowseHierarchy = MediaIDHelper.getHierarchy(
            current.getDescription().getMediaId());

    return Arrays.equals(newBrowserHeirarchy, currentBrowseHierarchy);
  }

  private void setCurrentQueueIndex(int index) {
    if (index >= 0 && index < mPlayingQueue.size()) {
      mCurrentIndex = index;
      mListener.onCurrentQueueIndexUpdated(mCurrentIndex);
    }
  }

  public boolean setCurrentQueueItem(long queueId) {
    int index = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, queueId);
    setCurrentQueueIndex(index);
    return index >= 0;
  }

  public boolean setCurrentQueueItem(String mediaId) {
    // set the current index on queue from the music Id:
    int index = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, mediaId);
    setCurrentQueueIndex(index);
    return index >= 0;
  }

  public boolean skipQueuePosition(int amount) {
    int index = mCurrentIndex + amount;

    if (index <= 0) {
      // skip backwards before the first song will keep you on the first song
      index = 0;
    } else if (mPlayingQueue.size() > 1) {
      // skip forwards when in last song will cycle back to start of the queue
      index %= mPlayingQueue.size();
    }

    if (!QueueHelper.isIndexPlayable(index, mPlayingQueue)) {
      return false;
    }

    mCurrentIndex = index;

    return true;
  }

//  public boolean setQueueFromSearch(String query, Bundle extras) {
//    List<MediaSessionCompat.QueueItem> queue =
//          QueueHelper.getPlayingQueueFromSearch(query, extras, mMusicProvider);
//
//    setCurrentQueue(mResources.getString(R.string.search_queue_titile), queue);
//    updateMetadata();
//
//    return queue != null && !queue.isEmpty();
//  }

  public void setRandomQueue() {
//    setCurrentQueue(mResources.getString(R.string.search_queue_titile),
//            QueueHelper.getRandomeQueue(mMusicProvider));
    updateMetadata();
  }

  public void setQueueFromMusic(String mediaId) {
    boolean canReuseQueue = false;

    // @TODO: This is a hack :P
    MediaMetadataCompat item = mMusicProvider.getMusic(mediaId);
    this.mPlayingQueue = new ArrayList<>();
    QueueHelper.addItemToQueue(item, this.mPlayingQueue);


    if (isSameBrowingCategory(mediaId)) {
      canReuseQueue = setCurrentQueueItem(mediaId);
    }

    if (!canReuseQueue) {
//      String queueTitle = mResources.getString(R.string.browse_musics_by_genre_subtitle,
//              MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaId));
//      setCurrentQueue(queueTitle,
//              QueueHelper.getPlayingQueue(mediaId, mMusicProvider), mediaId);
    }

    updateMetadata();
  }

  public MediaSessionCompat.QueueItem getCurrentMusic() {
    if (!QueueHelper.isIndexPlayable(mCurrentIndex, mPlayingQueue)) {
      return null;
    }

    return mPlayingQueue.get(mCurrentIndex);
  }

  public int getCurrentQueueSize() {
    if (mPlayingQueue == null) {
      return 0;
    }

    return mPlayingQueue.size();
  }

  protected void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue) {
    setCurrentQueue(title, newQueue, null);
  }

  protected void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue,
                                 String initialMediaId) {
    mPlayingQueue = newQueue;

    int index = 0;
    if (initialMediaId != null) {
      index = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, initialMediaId);
    }

    mCurrentIndex = Math.max(index, 0);
    mListener.onQueueUpdated(title, newQueue);
  }

  public void updateMetadata() {
    MediaSessionCompat.QueueItem currentMusic = getCurrentMusic();
    if (currentMusic == null) {
      mListener.onMetadataRetrieveError();
      return;
    }

//    final String musicId = MediaIDHelper.extractMusicIDFromMediaID(
//            currentMusic.getDescription().getMediaId());
    String musicId = currentMusic.getDescription().getMediaId();

    MediaMetadataCompat metadata = mMusicProvider.getMusic(musicId);
    if (metadata == null) {
      throw new IllegalArgumentException("Invalid musicId " + musicId);
    }

    mListener.onMetadataChanged(metadata);

    // @TODO: Handle Album art. We do it based on API
    // https://github.com/googlesamples/android-UniversalMusicPlayer/blob/master/mobile/src/main/java/com/example/android/uamp/playback/QueueManager.java#L197
  }

  public interface MetadataUpdateListener {
    void onMetadataChanged(MediaMetadataCompat metadata);
    void onMetadataRetrieveError();
    void onCurrentQueueIndexUpdated(int queueIndex);
    void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue);
  }
}
