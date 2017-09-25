package com.koalatea.thehollidayinn.softwareengineeringdaily.network;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.koalatea.thehollidayinn.softwareengineeringdaily.BuildConfig;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppScope;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;

import java.io.IOException;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Kurian on 25-Sep-17.
 */
@Module
public class NetworkModule {

    @VisibleForTesting
    final String BASE_URL = "https://software-enginnering-daily-api.herokuapp.com/api/";

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
    Interceptor providesHeaderInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder ongoing = chain.request().newBuilder();
                ongoing.addHeader("Accept", "application/json;versions=1");

                /*
                Timber.v("keithtest", userLogin.getToken());
                if (!userLogin.getToken().isEmpty()) {
                    ongoing.addHeader("Authorization", "Bearer " + userLogin.getToken());
                }
                */

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
    Retrofit providesRetrofitClient(@NonNull OkHttpClient httpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @AppScope
    APIInterface providesApiInterface(@NonNull Retrofit retrofit) {
        return retrofit.create(APIInterface.class);
    }

}
