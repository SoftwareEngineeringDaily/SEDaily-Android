package com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.User;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/*
 * Created by krh12 on 6/17/2017.
 */

public interface APIInterface {

    @GET("posts")
    Observable<List<Post>> getPosts(@QueryMap Map<String, String> options);

    @GET("posts/recommendations")
    Observable<List<Post>> getRecommendations(@QueryMap Map<String, String> options);

    @POST("posts/{postid}/upvote")
    Observable<Void> upVote(@Path("postid") String postId);

    @POST("posts/{postid}/downvote")
    Observable<Void> downVote(@Path("postid") String postId);

    @FormUrlEncoded
    @POST("auth/login")
    Observable<User> login(@Field("username") String username, @Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("auth/register")
    Observable<User> register(@Field("username") String username, @Field("email") String email, @Field("password") String password);

}
