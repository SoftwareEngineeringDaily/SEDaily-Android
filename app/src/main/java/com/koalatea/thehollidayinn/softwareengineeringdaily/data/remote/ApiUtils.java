package com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote;

import android.content.Context;

import com.koalatea.thehollidayinn.softwareengineeringdaily.BuildConfig;
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.UserRepository;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/*
 * Created by krh12 on 6/17/2017.
 */

public class ApiUtils {
  private static final String BASE_URL = "https://software-enginnering-daily-api.herokuapp.com/api/";

  private static final GsonConverterFactory gsonConverter = GsonConverterFactory.create();
  private static Retrofit retrofitAdapter;

  private static final Retrofit.Builder builder =
    new Retrofit.Builder()
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .addConverterFactory(gsonConverter)
      .baseUrl(BASE_URL);

  public static APIInterface getKibbleService(Context context) {
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    if (BuildConfig.DEBUG) {
      logging.setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    final UserRepository userLogin = UserRepository.getInstance(context);

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

    OkHttpClient client = clientbuilder.build();

    retrofitAdapter = builder
      .client(client)
      .build();

    return retrofitAdapter.create(APIInterface.class);
  }
}