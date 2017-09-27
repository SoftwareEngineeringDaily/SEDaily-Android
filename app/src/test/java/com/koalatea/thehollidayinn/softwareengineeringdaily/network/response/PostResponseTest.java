package com.koalatea.thehollidayinn.softwareengineeringdaily.network.response;

import com.google.gson.stream.JsonReader;
import com.koalatea.thehollidayinn.softwareengineeringdaily.test.BaseJsonParseUnitTest;

import org.junit.Test;

import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Kurian on 25-Sep-17.
 */
public class PostResponseTest extends BaseJsonParseUnitTest {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Test
    public void parsing_PostItem_no_auth_contains_expected_values() throws Exception {
        JsonReader reader = loadJson("200_post_response.json");

        PostResponse actual = gson.fromJson(reader, PostResponse.class);
        assertNotNull(actual);
        assertEquals("1234", actual.id());
        assertEquals("episode page link", actual.episodeLink());
        assertEquals("mp3 link", actual.audioLink());
        assertEquals("image link", actual.featuredImageLink());
        assertEquals("rendered content", actual.content().renderedContent());
        assertEquals("rendered title", actual.title().renderedTitle());
        assertEquals(sdf.parse("2017-09-25T02:00:53.000Z"), actual.date());
        assertEquals(0, actual.score());
        assertFalse(actual.upVoted());
        assertFalse(actual.downVoted());
    }

    @Test
    public void parsing_PostItem_with_auth_contains_expected_values() throws Exception {
        JsonReader reader = loadJson("200_post_response_logged_in.json");

        PostResponse actual = gson.fromJson(reader, PostResponse.class);
        assertNotNull(actual);
        assertEquals("1234", actual.id());
        assertEquals("episode page link", actual.episodeLink());
        assertEquals("mp3 link", actual.audioLink());
        assertEquals("image link", actual.featuredImageLink());
        assertEquals("rendered content", actual.content().renderedContent());
        assertEquals("rendered title", actual.title().renderedTitle());
        assertEquals(sdf.parse("2017-09-25T02:00:53.000Z"), actual.date());
        assertEquals(0, actual.score());
        assertTrue(actual.upVoted());
        assertTrue(actual.downVoted());
    }
}