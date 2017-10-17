package com.koalatea.thehollidayinn.softwareengineeringdaily.network.api;

import com.koalatea.thehollidayinn.softwareengineeringdaily.network.response.PostResponse;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by krh12 on 6/17/2017.
 */

public interface EpisodePostNetworkService {

    @GET("posts")
    Observable<List<PostResponse>> getAllPosts();

    @GET("posts?type=top")
    Observable<List<PostResponse>> getTopPosts();

    @GET("posts")
    Observable<List<PostResponse>> getPostsByCategory(@Query("categories") String categoryId);

    @GET("posts")
    Observable<List<PostResponse>> getPostBySearchString(@Query("search") String searchString);

    @GET("posts/recommendations")
    Observable<List<PostResponse>> getRecommendations();

    @POST("posts/{postid}/upvote")
    Observable<Void> upVote(@Path("postid") String postId);

    @POST("posts/{postid}/downvote")
    Observable<Void> downVote(@Path("postid") String postId);
}
