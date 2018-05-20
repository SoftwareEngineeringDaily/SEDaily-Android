package com.koalatea.thehollidayinn.softwareengineeringdaily.audio;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import static com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE;
import static com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MediaIDHelper.MEDIA_ID_MUSICS_BY_SEARCH;

/**
 * Created by keithholliday on 5/2/18.
 */

public class QueueHelper {

  private static final String TAG = "QueueHelper";

  private static final int RANDOM_QUEUE_SIZE = 10;

  public static List<MediaSessionCompat.QueueItem> getPlayingQueue(String mediaId,
                                                                   MusicProvider musicProvider) {
    // extract the browsing hierarchy from the media ID:
    String[] hierarchy = MediaIDHelper.getHierarchy(mediaId);

    if (hierarchy.length != 2) {
      return null;
    }

    String categoryType = hierarchy[0];
    String categoryValue = hierarchy[1];

    Iterable<MediaMetadataCompat> tracks = null;
    // This sample only supports genre and by_search category types.
    if (categoryType.equals(MEDIA_ID_MUSICS_BY_GENRE)) {
//      tracks = musicProvider.getMusicsByGenre(categoryValue);
    } else if (categoryType.equals(MEDIA_ID_MUSICS_BY_SEARCH)) {
//      tracks = musicProvider.searchMusicBySongTitle(categoryValue);
    }

    if (tracks == null) {
      return null;
    }

    return convertToQueue(tracks, hierarchy[0], hierarchy[1]);
  }

  public static int getMusicIndexOnQueue(Iterable<MediaSessionCompat.QueueItem> queue,
                                         String mediaId) {
    int index = 0;
    for (MediaSessionCompat.QueueItem item : queue) {
      if (mediaId.equals(item.getDescription().getMediaId())) {
        return index;
      }
      index++;
    }
    return -1;
  }

  public static int getMusicIndexOnQueue(Iterable<MediaSessionCompat.QueueItem> queue,
                                         long queueId) {
    int index = 0;
    for (MediaSessionCompat.QueueItem item : queue) {
      if (queueId == item.getQueueId()) {
        return index;
      }
      index++;
    }
    return -1;
  }

  private static List<MediaSessionCompat.QueueItem> convertToQueue(
          Iterable<MediaMetadataCompat> tracks, String... categories) {
    List<MediaSessionCompat.QueueItem> queue = new ArrayList<>();
    int count = 0;
    for (MediaMetadataCompat track : tracks) {

      // We create a hierarchy-aware mediaID, so we know what the queue is about by looking
      // at the QueueItem media IDs.
      String hierarchyAwareMediaID = MediaIDHelper.createMediaID(
              track.getDescription().getMediaId(), categories);

      MediaMetadataCompat trackCopy = new MediaMetadataCompat.Builder(track)
              .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
              .build();

      // We don't expect queues to change after created, so we use the item index as the
      // queueId. Any other number unique in the queue would work.
      MediaSessionCompat.QueueItem item = new MediaSessionCompat.QueueItem(
              trackCopy.getDescription(), count++);
      queue.add(item);
    }
    return queue;

  }

  public static void addItemToQueue(MediaMetadataCompat track, List<MediaSessionCompat.QueueItem> queue) {
//    String hierarchyAwareMediaID = MediaIDHelper.createMediaID(
//            track.getDescription().getMediaId(), "");
//
//    MediaMetadataCompat trackCopy = new MediaMetadataCompat.Builder(track)
//            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
//            .build();

    // We don't expect queues to change after created, so we use the item index as the
    // queueId. Any other number unique in the queue would work.
    MediaSessionCompat.QueueItem item = new MediaSessionCompat.QueueItem(
            track.getDescription(), 0);

    queue.add(item);
  }

//  public static List<MediaSessionCompat.QueueItem> getRandomQueue(MusicProvider musicProvider) {
//    List<MediaMetadataCompat> result = new ArrayList<>(RANDOM_QUEUE_SIZE);
//    Iterable<MediaMetadataCompat> shuffled = musicProvider.getShuffledMusic();
//    for (MediaMetadataCompat metadata: shuffled) {
//      if (result.size() == RANDOM_QUEUE_SIZE) {
//        break;
//      }
//      result.add(metadata);
//    }
//
//    return convertToQueue(result, MEDIA_ID_MUSICS_BY_SEARCH, "random");
//  }

  public static boolean isIndexPlayable(int index, List<MediaSessionCompat.QueueItem> queue) {
    return (queue != null && index >= 0 && index < queue.size());
  }

  public static boolean equals(List<MediaSessionCompat.QueueItem> list1,
                               List<MediaSessionCompat.QueueItem> list2) {
    if (list1 == list2) {
      return true;
    }
    if (list1 == null || list2 == null) {
      return false;
    }
    if (list1.size() != list2.size()) {
      return false;
    }
    for (int i=0; i<list1.size(); i++) {
      if (list1.get(i).getQueueId() != list2.get(i).getQueueId()) {
        return false;
      }
      if (!TextUtils.equals(list1.get(i).getDescription().getMediaId(),
              list2.get(i).getDescription().getMediaId())) {
        return false;
      }
    }
    return true;
  }

  public static boolean isQueueItemPlaying(Activity context,
                                           MediaSessionCompat.QueueItem queueItem) {
    // Queue item is considered to be playing or paused based on both the controller's
    // current media id and the controller's active queue item id
    MediaControllerCompat controller = MediaControllerCompat.getMediaController(context);
    if (controller != null && controller.getPlaybackState() != null) {
      long currentPlayingQueueId = controller.getPlaybackState().getActiveQueueItemId();
      String currentPlayingMediaId = controller.getMetadata().getDescription()
              .getMediaId();
      String itemMusicId = MediaIDHelper.extractMusicIDFromMediaID(
              queueItem.getDescription().getMediaId());
      if (queueItem.getQueueId() == currentPlayingQueueId
              && currentPlayingMediaId != null
              && TextUtils.equals(currentPlayingMediaId, itemMusicId)) {
        return true;
      }
    }
    return false;
  }
}
