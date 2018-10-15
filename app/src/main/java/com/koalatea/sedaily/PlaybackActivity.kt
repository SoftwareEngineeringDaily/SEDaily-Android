package com.koalatea.sedaily

import android.annotation.SuppressLint
import android.content.ComponentName
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.appcompat.app.AppCompatActivity
import com.koalatea.sedaily.media.MusicService
import com.koalatea.sedaily.media.library.PodcastSource
import com.koalatea.sedaily.models.DownloadDao
import com.koalatea.sedaily.models.Episode

@SuppressLint("Registered")
open class PlaybackActivity : AppCompatActivity() {
    private var mMediaBrowser: MediaBrowserCompat? = null
    private val mConnectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            try {
                connectToSession(mMediaBrowser?.sessionToken)
            } catch (e: RemoteException) {
//                hidePlaybackControls(null)
            }

        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
        }
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.apply {
                updateWithMeta(metadata)
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
//            if (shouldShowControls()) {
//                showPlaybackControls(state)
//                return
//            }
//
//            hidePlaybackControls(state)
        }
    }

    protected fun setUp() {
        if (mMediaBrowser == null) {
            mMediaBrowser = MediaBrowserCompat(SEDApp.appContext,
                    ComponentName(SEDApp.appContext, MusicService::class.java),
                    mConnectionCallbacks, null).apply { connect() }
        }
    }

    fun playMedia(episode: Episode) {
        val item: MediaMetadataCompat  = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, episode._id)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, episode.mp3)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, episode.title?.rendered)
                .build()

        val bItem = MediaBrowserCompat.MediaItem(item.description,
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)

        PodcastSource.setItem(item)

//        val podcastSessionStateManager = PodcastSessionStateManager.getInstance()
//        val currentPLayingTitle = podcastSessionStateManager.getCurrentTitle()
//        val isSameMedia = currentPLayingTitle == post.getTitle().getRendered()
        val isSameMedia = false
//
        onMediaItemSelected(bItem, isSameMedia)
    }

    // Local play
    fun playMedia(episode: DownloadDao.DownloadEpisode) {
        // @TODO: How do we play local?
        val item: MediaMetadataCompat  = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, episode.postId)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, episode.filename)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, episode.title)
                .build()

        val bItem = MediaBrowserCompat.MediaItem(item.description,
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)

        PodcastSource.setItem(item)

//        val podcastSessionStateManager = PodcastSessionStateManager.getInstance()
//        val currentPLayingTitle = podcastSessionStateManager.getCurrentTitle()
//        val isSameMedia = currentPLayingTitle == post.getTitle().getRendered()
        val isSameMedia = false
//
        onMediaItemSelected(bItem, isSameMedia)
    }

    @Throws(RemoteException::class)
    private fun connectToSession(token: MediaSessionCompat.Token?) {
        val mediaController = MediaControllerCompat(
                this, token!!)

        MediaControllerCompat.setMediaController(this, mediaController)
        mediaController.registerCallback(controllerCallback)

//        if (shouldShowControls()) {
//            showPlaybackControls(null)
//        } else {
//            hidePlaybackControls(null)
//        }
//
//        if (mControlsFragment != null) {
//            mControlsFragment.onConnected()
//        }

        //        @TODO: onMediaControllerConnected();
    }

    private fun onMediaItemSelected(item: MediaBrowserCompat.MediaItem, isSameMedia: Boolean) {
        if (!item.isPlayable) return

        val controller = MediaControllerCompat.getMediaController(this) ?: return

        val controls = controller.transportControls

        if (!isSameMedia) {
            controls.playFromMediaId(item.mediaId, null)
            return
        }

        val state = controller.playbackState.state

        if (state == PlaybackStateCompat.STATE_PLAYING) {
            controls.pause()
            return
        }

        controls.play()
    }

    private fun updateWithMeta(metadata: MediaMetadataCompat) {
//        if (mControlsFragment == null) return
//        mControlsFragment.updateWithMeta(metadata)
    }
}