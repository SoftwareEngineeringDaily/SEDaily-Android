package com.koalatea.thehollidayinn.softwareengineeringdaily.network;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.koalatea.thehollidayinn.softwareengineeringdaily.BuildConfig;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppScope;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.preference.AuthPreference;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.api.AuthNetworkService;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.api.EpisodePostNetworkService;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.response.ResponseAdapterFactory;

import java.io.IOException;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Created by Kurian on 25-Sep-17.
 */
@Module
public class NetworkModule {

    @VisibleForTesting
    final String BASE_URL = "https://software-enginnering-daily-api.herokuapp.com/api/";

    @Provides
    @AppScope
    Gson providesGson() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(ResponseAdapterFactory.create())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();
    }

    @Provides
    @AppScope
    HttpLoggingInterceptor.Level proviesHttpLoggingLevel() {
        if(BuildConfig.DEBUG) {
            return HttpLoggingInterceptor.Level.BODY;
        } else {
            return HttpLoggingInterceptor.Level.NONE;
        }
    }

    @Provides
    @AppScope
    HttpLoggingInterceptor providesHttpLoggingInterceptor(@NonNull HttpLoggingInterceptor.Level level) {
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        return logger.setLevel(level);
    }

    @Provides
    @AppScope
    Interceptor providesHeaderInterceptor(@NonNull final AuthPreference authPreference) {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder ongoing = chain.request().newBuilder();
                ongoing.addHeader("Accept", "application/json;versions=1");
                if (authPreference.isLoggedIn()) {
                    Timber.v("keithtest", authPreference.getToken());
                    ongoing.addHeader("Authorization", "Bearer " + authPreference.getToken());
                }
                return chain.proceed(ongoing.build());
            }
        };
    }

    @Provides
    @AppScope
    OkHttpClient providesHttpClient(@NonNull HttpLoggingInterceptor loggingInterceptor,
                                    @NonNull Interceptor headerInterceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(headerInterceptor)
                .build();
    }

    @Provides
    @AppScope
    Retrofit providesRetrofitClient(@NonNull OkHttpClient httpClient, @NonNull Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    @AppScope
    APIInterface providesApiInterface(@NonNull Retrofit retrofit) {
        return retrofit.create(APIInterface.class);
    }

    @Provides
    @AppScope
    EpisodePostNetworkService providesPostNetworkService(@NonNull Retrofit retrofit) {
        return retrofit.create(EpisodePostNetworkService.class);
    }

    @Provides
    @AppScope
    AuthNetworkService providesAuthNetworkService(@NonNull Retrofit retrofit) {
        return retrofit.create(AuthNetworkService.class);
    }
}
