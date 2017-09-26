package com.koalatea.thehollidayinn.softwareengineeringdaily.network.api;

import com.koalatea.thehollidayinn.softwareengineeringdaily.network.response.AuthResponse;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by krh12 on 6/17/2017.
 */

public interface AuthNetworkService {

    @FormUrlEncoded
    @POST("auth/login")
    Single<AuthResponse> login(@Field("username") String username,
                               @Field("password") String password);

    @FormUrlEncoded
    @POST("auth/register")
    Single<AuthResponse> register(@Field("username") String username,
                                      @Field("password") String password);

}
