package com.koalatea.thehollidayinn.softwareengineeringdaily.audio;

/*
  Created by krh12 on 6/16/2017.
 */

// @TODO: Fix this to use local data

import android.os.AsyncTask;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.LinkedHashMap;

import timber.log.Timber;

public class MusicProvider {

    private static final String TAG = MusicProvider.class.getSimpleName();

    public static final String MEDIA_ID_ROOT = "__ROOT__";
    public static final String MEDIA_ID_EMPTY_ROOT = "__EMPTY__";

    private static final String CATALOG_URL =
            "https://storage.googleapis.com/automotive-media/music.json";

    private static final String JSON_MUSIC = "music";
    private static final String JSON_TITLE = "title";
    private static final String JSON_ALBUM = "album";
    private static final String JSON_ARTIST = "artist";
    private static final String JSON_GENRE = "genre";
    private static final String JSON_SOURCE = "source";
    private static final String JSON_IMAGE = "image";
    private static final String JSON_TRACK_NUMBER = "trackNumber";
    private static final String JSON_TOTAL_TRACK_COUNT = "totalTrackCount";
    private static final String JSON_DURATION = "duration";

    // Categorized caches for music track data:
    private static LinkedHashMap<String, MediaMetadataCompat> mMusicListById;

    private enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    private static MusicProvider instance;


    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    private MusicProvider() {
        mMusicListById = new LinkedHashMap<>();
    }

    public static MusicProvider getInstance() {
        if (instance == null) {
            instance = new MusicProvider();
        }

        return instance;
    }

    public Iterable<MediaMetadataCompat> getAllMusics() {
        if (mCurrentState != State.INITIALIZED || mMusicListById.isEmpty()) {
            return Collections.emptyList();
        }
        return mMusicListById.values();
    }

    public MediaMetadataCompat getMusic(String musicId) {
        return mMusicListById.containsKey(musicId) ? mMusicListById.get(musicId) : null;
    }

    public synchronized void updateMusic(String musicId, MediaMetadataCompat metadata) {
        // @TODO: Do we need to check this?
        //MediaMetadataCompat track = mMusicListById.get(musicId);
        mMusicListById.put(musicId, metadata);
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }


    public void retrieveMediaAsync(final Callback callback) {
        if (mCurrentState == State.INITIALIZED) {
            // Already initialized, so call back immediately.
            if (callback == null) return;
            callback.onMusicCatalogReady(true);
            return;
        }

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... params) {
                retrieveMedia();
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State current) {
                if (callback != null) {
                    callback.onMusicCatalogReady(current == State.INITIALIZED);
                }
            }
        }.execute();
    }

    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                int slashPos = CATALOG_URL.lastIndexOf('/');
                String path = CATALOG_URL.substring(0, slashPos + 1);
                JSONObject jsonObj = fetchJSONFromUrl(CATALOG_URL);
                if (jsonObj == null) {
                    return;
                }
                JSONArray tracks = jsonObj.getJSONArray(JSON_MUSIC);
                if (tracks != null) {
                    for (int j = tracks.length() - 1; j >= 0; j--) {
                        MediaMetadataCompat item = buildFromJSON(tracks.getJSONObject(j), path);
                        String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                        mMusicListById.put(musicId, item);
                    }
                }
                mCurrentState = State.INITIALIZED;
            }
        } catch (JSONException jsonException) {
            Log.e(TAG, "Could not retrieve music list", jsonException);
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }

    private MediaMetadataCompat buildFromJSON(JSONObject json, String basePath)
            throws JSONException {

        String title = json.getString(JSON_TITLE);
        String album = json.getString(JSON_ALBUM);
        String artist = json.getString(JSON_ARTIST);
        String genre = json.getString(JSON_GENRE);
        String source = json.getString(JSON_SOURCE);
        String iconUrl = json.getString(JSON_IMAGE);
        int trackNumber = json.getInt(JSON_TRACK_NUMBER);
        int totalTrackCount = json.getInt(JSON_TOTAL_TRACK_COUNT);
        int duration = json.getInt(JSON_DURATION) * 1000; // ms

        // Media is stored relative to JSON file
        if (!source.startsWith("https")) {
            source = basePath + source;
        }
        if (!iconUrl.startsWith("https")) {
            iconUrl = basePath + iconUrl;
        }
        // Since we don't have a unique ID in the server, we fake one using the hashcode of
        // the music source. In a real world app, this could come from the server.
        String id = String.valueOf(source.hashCode());

        // Adding the music source to the MediaMetadata (and consequently using it in the
        // mediaSession.setMetadata) is not a good idea for a real world music app, because
        // the session metadata can be accessed by notification listeners. This is done in this
        // sample for convenience only.
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build();
    }

    private JSONObject fetchJSONFromUrl(String urlString) {
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return new JSONObject(stringBuilder.toString());
        } catch (IOException | JSONException exception) {
            Log.e(TAG, "Failed to parse the json for media list", exception);
            return null;
        } finally {
            // If the inputStream was opened, try to close it now.
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                    // Ignore the exception since there's nothing left to do with the stream
                }
            }
        }
    }
}
