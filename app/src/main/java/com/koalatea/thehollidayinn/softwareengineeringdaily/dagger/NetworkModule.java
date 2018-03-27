package com.koalatea.thehollidayinn.softwareengineeringdaily.dagger;

import android.app.Application;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.koalatea.thehollidayinn.softwareengineeringdaily.BuildConfig;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.UserRepository;

import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by keithholliday on 1/3/18.
 */

@Module
public class NetworkModule {
    String baseUrl =  "https://software-enginnering-daily-api.herokuapp.com/api/";
    // Staging
//    String baseUrl = "https://sedaily-backend-staging.herokuapp.com/api/";

    // Local
//    String baseUrl = "http://192.168.1.251:4040/api/";

//    public NetworkModule(String baseUrl) {
//        this.baseUrl = baseUrl;
//    }

    @Provides
    @Singleton
    Cache provideHttpCache(Application application) {
        int cacheSize = 10 * 1024 * 1024;
        Cache cache = new Cache(application.getCacheDir(), cacheSize);
        return cache;
    }

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return gsonBuilder.create();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkhttpClient(Cache cache) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        }
        final UserRepository userLogin = UserRepository.getInstance(SEDApp.component.context());
        OkHttpClient.Builder clientbuilder = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request.Builder ongoing = chain.request().newBuilder();
                        ongoing.addHeader("Accept", "application/json;versions=1");

                        if (!userLogin.getToken().isEmpty()) {
                            ongoing.addHeader("Authorization", "Bearer " + userLogin.getToken());
                        }

                        return chain.proceed(ongoing.build());
                    }
                });

        if (BuildConfig.DEBUG) {
            clientbuilder.addInterceptor(logging);
        }
//        clientbuilder.cache(cache);

        return clientbuilder.build();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(Gson gson, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build();
    }

    @Provides
    APIInterface providesKibbleService(Retrofit retrofitAdapter) {
        return retrofitAdapter.create(APIInterface.class);
    }
}
