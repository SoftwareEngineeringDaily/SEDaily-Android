package com.koalatea.thehollidayinn.softwareengineeringdaily.network.api;

import com.koalatea.thehollidayinn.softwareengineeringdaily.network.response.AuthResponse;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by krh12 on 6/17/2017.
 */

public interface AuthNetworkService {

    @FormUrlEncoded
    @POST("auth/login")
    Observable<AuthResponse> login(@Field("username") String username,
                                   @Field("password") String password);

    @FormUrlEncoded
    @POST("auth/register")
    Observable<AuthResponse> register(@Field("username") String username,
                                      @Field("password") String password);

}
