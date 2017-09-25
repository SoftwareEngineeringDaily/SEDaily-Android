package com.koalatea.thehollidayinn.softwareengineeringdaily.network.api;

import com.koalatea.thehollidayinn.softwareengineeringdaily.network.response.PostResponse;

import java.util.List;
import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by krh12 on 6/17/2017.
 */

public interface EpisodePostNetworkService {

    @GET("posts")
    Observable<List<PostResponse>> getPosts(@QueryMap Map<String, String> options);

    @GET("posts/recommendations")
    Observable<List<PostResponse>> getRecommendations(@QueryMap Map<String, String> options);

    @POST("posts/{postid}/upvote")
    Observable<Void> upVote(@Path("postid") String postId);

    @POST("posts/{postid}/downvote")
    Observable<Void> downVote(@Path("postid") String postId);
}
