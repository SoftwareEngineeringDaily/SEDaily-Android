package com.koalatea.thehollidayinn.softwareengineeringdaily.domain;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.mapper.PostItemMapper;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.PostItem;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.api.EpisodePostNetworkService;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.response.PostResponse;
import com.koalatea.thehollidayinn.softwareengineeringdaily.test.BaseUnitTest;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by Kurian on 26-Sep-17.
 */
public class PostRepositoryImplTest extends BaseUnitTest {

    PostRepositoryImpl repo;

    @Mock
    EpisodePostNetworkService api;

    @Spy
    PostItemMapper mapper;

    @Override
    public void setUp() {
        super.setUp();
        repo = new PostRepositoryImpl(api, mapper);
    }

    @Test
    public void getPosts_returns_list_of_post_items_with_expected_count() throws Exception {
        final List<PostResponse> inputs = createPostResponseList(5);
        doReturn(Observable.just(inputs)).when(api).getAllPosts();
        repo.getPosts().test().assertNoErrors().assertValue(new Predicate<List<PostItem>>() {
            @Override
            public boolean test(@NonNull List<PostItem> postItems) throws Exception {
                return postItems.size() == inputs.size();
            }
        });
    }

    @Test
    public void getPostByCategory_returns_list_of_post_items_with_expected_count() throws Exception {
        final List<PostResponse> inputs = createPostResponseList(3);
        doReturn(Observable.just(inputs)).when(api).getPostsByCategory(anyString());
        repo.getPostByCategory("category")
                .test().assertNoErrors().assertValue(new Predicate<List<PostItem>>() {
            @Override
            public boolean test(@NonNull List<PostItem> postItems) throws Exception {
                return postItems.size() == inputs.size();
            }
        });

        verify(api).getPostsByCategory(eq("category"));
    }

    @Test
    public void findPosts_returns_list_of_post_items_with_expected_count() throws Exception {
        final List<PostResponse> inputs = createPostResponseList(3);
        doReturn(Observable.just(inputs)).when(api).getPostBySearchString(anyString());
        repo.findPosts("my posts")
                .test().assertNoErrors().assertValue(new Predicate<List<PostItem>>() {
            @Override
            public boolean test(@NonNull List<PostItem> postItems) throws Exception {
                return postItems.size() == inputs.size();
            }
        });
        verify(api).getPostBySearchString(eq("my posts"));
    }

    @Test
    public void getTopPosts_returns_list_of_post_items_with_expected_count() throws Exception {
        final List<PostResponse> inputs = createPostResponseList(4);
        doReturn(Observable.just(inputs)).when(api).getTopPosts();
        repo.getTopPosts()
                .test().assertNoErrors().assertValue(new Predicate<List<PostItem>>() {
            @Override
            public boolean test(@NonNull List<PostItem> postItems) throws Exception {
                return postItems.size() == inputs.size();
            }
        });
        verify(api).getTopPosts();
    }

    @Test
    public void getRecommendedPosts_returns_list_of_post_items_with_expected_count() throws Exception {
        final List<PostResponse> inputs = createPostResponseList(2);
        doReturn(Observable.just(inputs)).when(api).getRecommendations();
        repo.getRecommendedPosts()
                .test().assertNoErrors().assertValue(new Predicate<List<PostItem>>() {
            @Override
            public boolean test(@NonNull List<PostItem> postItems) throws Exception {
                return postItems.size() == inputs.size();
            }
        });
        verify(api).getRecommendations();
    }

    @Test
    public void upvote_invokes_analytics_upvote_event() throws Exception {
        doReturn(Completable.complete()).when(api).upVote(anyString());
        repo.upVote("post id").test().assertNoErrors();
        verify(appComponent().analyticsFacade()).trackUpVote(eq("post id"));
    }

    @Test
    public void downvote_invokes_analytics_downvote_event() throws Exception {
        doReturn(Completable.complete()).when(api).downVote(anyString());
        repo.downVote("post id").test().assertNoErrors();
        verify(appComponent().analyticsFacade()).trackDownVote(eq("post id"));
    }

    private List<PostResponse> createPostResponseList(int count) {

        PostResponse.TitleResponse mockTitle = mock(PostResponse.TitleResponse.class);
        doReturn("title").when(mockTitle).renderedTitle();
        PostResponse.ContentResponse mockContent = mock(PostResponse.ContentResponse.class);
        doReturn("content").when(mockContent).renderedContent();
        Date now = new Date();
        List<PostResponse> items = new ArrayList<>(count);

        for(int i = 0; i < count; i++) {
            items.add(PostResponse.create(String.valueOf(i), now, "episode link", "audio link",
                    "featured link", mockContent, mockTitle, 0, false, true));
        }
        return items;
    }
}