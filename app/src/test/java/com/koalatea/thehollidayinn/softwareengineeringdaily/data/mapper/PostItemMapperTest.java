package com.koalatea.thehollidayinn.softwareengineeringdaily.data.mapper;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.PostItem;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.response.PostResponse;
import com.koalatea.thehollidayinn.softwareengineeringdaily.test.BaseUnitTest;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created by Kurian on 26-Sep-17.
 */
public class PostItemMapperTest extends BaseUnitTest {

    private PostItemMapper mapper;

    @Override
    public void setUp() {
        super.setUp();
        mapper = new PostItemMapper();
    }

    @Test
    public void map_returns_expected_post_item() throws Exception {
        PostResponse.TitleResponse mockTitle = mock(PostResponse.TitleResponse.class);
        doReturn("title").when(mockTitle).renderedTitle();
        PostResponse.ContentResponse mockContent = mock(PostResponse.ContentResponse.class);
        doReturn("content").when(mockContent).renderedContent();
        Date now = new Date();
        PostResponse input = PostResponse.create("id", now, "episode link", "audio link",
                "featured link", mockContent, mockTitle, 0, false, true);

        PostItem actual = mapper.map(input);
        assertEquals("id", actual.id());
        assertEquals(now, actual.date());
        assertEquals("episode link", actual.episodeLink());
        assertEquals("audio link", actual.audioLink());
        assertEquals("featured link", actual.featuredImgLink());
        assertEquals("content", actual.content());
        assertEquals("title", actual.title());
        assertTrue(actual.downVoted());
        assertFalse(actual.upVoted());
    }

    @Test
    public void map_collection_returns_expected_number_of_items() throws Exception {
        PostResponse.TitleResponse mockTitle = mock(PostResponse.TitleResponse.class);
        doReturn("title").when(mockTitle).renderedTitle();
        PostResponse.ContentResponse mockContent = mock(PostResponse.ContentResponse.class);
        doReturn("content").when(mockContent).renderedContent();
        Date now = new Date();
        PostResponse input = PostResponse.create("id", now, "episode link", "audio link",
                "featured link", mockContent, mockTitle, 0, false, true);

        List<PostResponse> inputList = Arrays.asList(input, input, input, input, input);
        List<PostItem> outputList = mapper.mapAll(inputList);
        assertEquals(inputList.size(), outputList.size());
    }

    @Test
    public void map_collection_returns_empty_list_with_null_input() throws Exception {
        List<PostItem> outputList = mapper.mapAll(null);
        assertEquals(0, outputList.size());
    }

    @Test
    public void map_collection_returns_empty_list_with_empty_list_input() throws Exception {
        List<PostItem> outputList = mapper.mapAll(new ArrayList<PostResponse>());
        assertEquals(0, outputList.size());
    }
}