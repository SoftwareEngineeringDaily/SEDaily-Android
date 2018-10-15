/*
 * Copyright 2018 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.koalatea.sedaily.media

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.koalatea.sedaily.media.extensions.album
import com.koalatea.sedaily.media.extensions.id
import com.koalatea.sedaily.media.extensions.toMediaSource
import com.koalatea.sedaily.media.extensions.trackNumber
import com.koalatea.sedaily.media.library.MusicSource

/**
 * Class to bridge UAMP to the ExoPlayer MediaSession extension.
 */
class UampPlaybackPreparer(
        private val musicSource: MusicSource,
        private val exoPlayer: ExoPlayer,
        private val dataSourceFactory: DataSource.Factory
) : MediaSessionConnector.PlaybackPreparer {

    /**
     * UAMP supports preparing (and playing) from search, as well as media ID, so those
     * capabilities are declared here.
     *
     * TODO: Add support for ACTION_PREPARE and ACTION_PLAY, which mean "prepare/play something".
     */
    override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

    override fun onPrepare() = Unit

    /**
     * Handles callbacks to both [MediaSessionCompat.Callback.onPrepareFromMediaId]
     * *AND* [MediaSessionCompat.Callback.onPlayFromMediaId] when using [MediaSessionConnector].
     * This is done with the expectation that "play" is just "prepare" + "play".
     *
     * If your app needs to do something special for either 'prepare' or 'play', it's possible
     * to check [ExoPlayer.getPlayWhenReady]. If this returns `true`, then it's
     * [MediaSessionCompat.Callback.onPlayFromMediaId], otherwise it's
     * [MediaSessionCompat.Callback.onPrepareFromMediaId].
     */
    override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
        musicSource.whenReady {
            val itemToPlay: MediaMetadataCompat? = musicSource.find { item ->
                item.id == mediaId
            }

            if (itemToPlay == null) {
                Log.w(TAG, "Content not found: MediaID=$mediaId")

                // TODO: Notify caller of the error.
            } else {
                val metadataList = buildPlaylist(itemToPlay)
                val mediaSource = metadataList.toMediaSource(dataSourceFactory)

                // Since the playlist was probably based on some ordering (such as tracks
                // on an album), find which window index to play first so that the song the
                // user actually wants to hear plays first.
                val initialWindowIndex = metadataList.indexOf(itemToPlay)

                exoPlayer.prepare(mediaSource)
                exoPlayer.seekTo(initialWindowIndex, 0)
                exoPlayer.addListener(object: Player.EventListener {
                    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
                         //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onSeekProcessed() {
                         //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                         //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onPlayerError(error: ExoPlaybackException?) {
                         //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onLoadingChanged(isLoading: Boolean) {
                         //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onPositionDiscontinuity(reason: Int) {
                         //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onRepeatModeChanged(repeatMode: Int) {
                         //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                         //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
                         //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                         //To change body of created functions use File | Settings | File Templates.
                    }

                })
            }
        }
    }

    /**
     * Handles callbacks to both [MediaSessionCompat.Callback.onPrepareFromSearch]
     * *AND* [MediaSessionCompat.Callback.onPlayFromSearch] when using [MediaSessionConnector].
     * (See above for details.)
     *
     * This method is used by the Google Assistant to respond to requests such as:
     * - Play Geisha from Wake Up on UAMP
     * - Play electronic music on UAMP
     * - Play music on UAMP
     *
     * For details on how search is handled, see [AbstractMusicSource.search].
     */
    override fun onPrepareFromSearch(query: String?, extras: Bundle?) {
        musicSource.whenReady {
            val metadataList = musicSource.search(query ?: "", extras ?: Bundle.EMPTY)
            if (metadataList.isNotEmpty()) {
                val mediaSource = metadataList.toMediaSource(dataSourceFactory)
                exoPlayer.prepare(mediaSource)
            }
        }
    }

    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) = Unit

    override fun getCommands(): Array<String>? = null

    override fun onCommand(
            player: Player?,
            command: String?,
            extras: Bundle?,
            cb: ResultReceiver?
    ) = Unit

    /**
     * Builds a playlist based on a [MediaMetadataCompat].
     *
     * TODO: Support building a playlist by artist, genre, etc...
     *
     * @param item Item to base the playlist on.
     * @return a [List] of [MediaMetadataCompat] objects representing a playlist.
     */
    private fun buildPlaylist(item: MediaMetadataCompat): List<MediaMetadataCompat> =
            musicSource.filter { it.album == item.album }.sortedBy { it.trackNumber }
}

private const val TAG = "MediaSessionHelper"
