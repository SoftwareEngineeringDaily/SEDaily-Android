package com.koalatea.thehollidayinn.softwareengineeringdaily;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.LocalPlayback;
import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MusicProvider;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.koalatea.thehollidayinn.softwareengineeringdaily.debug", appContext.getPackageName());
    }

    @Test
    public void testPlayback() throws InterruptedException {
        Context appContext = InstrumentationRegistry.getTargetContext();
        MusicProvider mMusicProvider = MusicProvider.getInstance();
        LocalPlayback mLocalPlayback = new LocalPlayback(appContext, mMusicProvider);

        int stateResult = mLocalPlayback.getState();
        assertEquals(stateResult, PlaybackStateCompat.STATE_NONE);

        int mediaId = 123;
        MediaDescriptionCompat mediaDescription = new MediaDescriptionCompat.Builder()
                .setDescription("Testing")
                .setTitle("Title")
                .setMediaId(String.valueOf(mediaId))
                .build();

        MediaMetadataCompat mediaMetadataCompat = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, "https://19733.mc.tritondigital.com/NPR_510289/media-session/5708daa1-4343-4f4e-88ed-89746a7b8eda/anon.npr-mp3/npr/pmoney/2018/09/20180905_pmoney_pmpod729rerun.mp3")
                .build();
        mMusicProvider.updateMusic(String.valueOf(mediaId), mediaMetadataCompat);

        MediaSessionCompat.QueueItem queueItem = new MediaSessionCompat.QueueItem(mediaDescription, mediaId);
        mLocalPlayback.play(queueItem);

        Thread.sleep(2000);

        int stateResult2 = mLocalPlayback.getState();
        assertEquals(PlaybackStateCompat.STATE_PLAYING, stateResult2);
    }
}
