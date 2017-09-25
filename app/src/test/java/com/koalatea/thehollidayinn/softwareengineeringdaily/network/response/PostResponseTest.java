package com.koalatea.thehollidayinn.softwareengineeringdaily.network.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.koalatea.thehollidayinn.softwareengineeringdaily.base.BaseJsonParseTest;

import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Kurian on 25-Sep-17.
 */
public class PostResponseTest extends BaseJsonParseTest{

    Gson gson;

    @Before
    public void setUp() {
        //Possibly receive this via DI rather than manual instance to keep consistency
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .registerTypeAdapterFactory(ResponseTypeAdaptorFactory.create())
                .create();
    }

    @Test
    public void parsing_PostItem_contains_expected_values() throws Exception {
        JsonReader reader = loadJson("post_response.json");
        PostResponse actual = gson.fromJson(reader, PostResponse.class);
        assertNotNull(actual);
        assertEquals("1234", actual.id());
        assertEquals("episode page link", actual.episodeLink());
        assertEquals("mp3 link", actual.audioLink());
        assertEquals("image link", actual.featuredImageLink());
        assertEquals("rendered content", actual.content().renderedContent());
        assertEquals("rendered title", actual.title().renderedTitle());
    }

    protected JsonReader loadJson(String filename) throws FileNotFoundException {
        return new JsonReader(new FileReader(loadJsonFile(filename)));
    }
}