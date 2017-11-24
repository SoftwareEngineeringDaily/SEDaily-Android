package com.koalatea.thehollidayinn.softwareengineeringdaily.domain;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SDEApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.mapper.PostItemMapper;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.PostItem;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.api.EpisodePostNetworkService;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.response.PostResponse;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * Created by Kurian on 26-Sep-17.
 */

class PostRepositoryImpl implements PostRepository {


    @VisibleForTesting
    private final EpisodePostNetworkService api;
    @VisibleForTesting
    private final PostItemMapper mapper;

  PostRepositoryImpl(@NonNull EpisodePostNetworkService api, @NonNull PostItemMapper mapper) {
        this.api = api;
        this.mapper = mapper;
    }

    @Override
    public Observable<List<PostItem>> getPosts() {
        return api.getAllPosts()
                .map(new Function<List<PostResponse>, List<PostItem>>() {
                    @Override
                    public List<PostItem> apply(@NonNull List<PostResponse> postResponses)
                            throws Exception {
                        return mapper.mapAll(postResponses);
                    }
                });
    }

    @Override
    public Observable<List<PostItem>> getPostByCategory(@NonNull String categoryId) {
        return api.getPostsByCategory(categoryId)
                .map(new Function<List<PostResponse>, List<PostItem>>() {
                    @Override
                    public List<PostItem> apply(@NonNull List<PostResponse> postResponses)
                            throws Exception {
                        return mapper.mapAll(postResponses);
                    }
                });
    }

    @Override
    public Observable<List<PostItem>> findPosts(@NonNull String filter) {
        return api.getPostBySearchString(filter)
                .map(new Function<List<PostResponse>, List<PostItem>>() {
                    @Override
                    public List<PostItem> apply(@NonNull List<PostResponse> postResponses)
                            throws Exception {
                        return mapper.mapAll(postResponses);
                    }
                });
    }

  @Override
  public Completable upVote(@NonNull final String postId) {
    return api.upVote(postId).doOnComplete(new Action() {
      @Override
      public void run() throws Exception {
        SDEApp.component().analyticsFacade().trackUpVote(postId);
      }
    });
  }

  @Override
  public Completable downVote(@NonNull final String postId) {
    return api.downVote(postId).doOnComplete(new Action() {
      @Override
      public void run() throws Exception {
        SDEApp.component().analyticsFacade().trackDownVote(postId);
      }
    });
  }

    @Override
    public Observable<List<PostItem>> getTopPosts() {
        return api.getTopPosts()
                .map(new Function<List<PostResponse>, List<PostItem>>() {
                    @Override
                    public List<PostItem> apply(@NonNull List<PostResponse> postResponses)
                            throws Exception {
                        return mapper.mapAll(postResponses);
                    }
                });
    }

    @Override
    public Observable<List<PostItem>> getRecommendedPosts() {
        return api.getRecommendations()
                .map(new Function<List<PostResponse>, List<PostItem>>() {
                    @Override
                    public List<PostItem> apply(@NonNull List<PostResponse> postResponses)
                            throws Exception {
                        return mapper.mapAll(postResponses);
                    }
                });
    }
}
