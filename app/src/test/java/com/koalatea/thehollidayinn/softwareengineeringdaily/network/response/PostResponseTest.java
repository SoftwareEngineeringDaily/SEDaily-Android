package com.koalatea.thehollidayinn.softwareengineeringdaily.network.response;

import com.google.gson.stream.JsonReader;
import com.koalatea.thehollidayinn.softwareengineeringdaily.test.BaseJsonParseTest;

import org.junit.Test;

import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Kurian on 25-Sep-17.
 */
public class PostResponseTest extends BaseJsonParseTest{

    @Test
    public void parsing_PostItem_contains_expected_values() throws Exception {
        JsonReader reader = loadJson("200_post_response.json");

        PostResponse actual = gson.fromJson(reader, PostResponse.class);
        assertNotNull(actual);
        assertEquals("1234", actual.id());
        assertEquals("episode page link", actual.episodeLink());
        assertEquals("mp3 link", actual.audioLink());
        assertEquals("image link", actual.featuredImageLink());
        assertEquals("rendered content", actual.content().renderedContent());
        assertEquals("rendered title", actual.title().renderedTitle());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .parse("2017-09-25T02:00:53.000Z"), actual.date());
        assertEquals(0, actual.score());
    }
}