package com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote;

import android.content.Context;
import android.util.Log;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.UserRepository;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by krh12 on 6/17/2017.
 */


public class ApiUtils {
//    public static final String BASE_URL = "http://10.1.10.107:3000/api/v1/";

    public static final String BASE_URL = "https://software-enginnering-daily-api.herokuapp.com/api/";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static GsonConverterFactory gsonConverter = GsonConverterFactory.create();
    private static Retrofit retrofitAdapter;

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(gsonConverter)
                    .baseUrl(BASE_URL);

    public static APIInterface getKibbleService(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        if (BuildConfig.DEBUG) {
        //logging.setLevel(HttpLoggingInterceptor.Level.BODY);
//        }

        final UserRepository userLogin = UserRepository.getInstance(context);

        OkHttpClient client = new OkHttpClient.Builder()
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
                })
//                .addInterceptor(logging)
                .build();

        retrofitAdapter = builder
                .client(client)
                .build();


        return retrofitAdapter.create(APIInterface.class);
    }

    public static ErrorResponse getErrorResponse(HttpException error) {
        retrofit2.Response<?> response = error.response();
        Converter<ResponseBody, ?> errorConverter =
                gsonConverter
                        .responseBodyConverter(ErrorResponse.class, new Annotation[0], retrofitAdapter);
        try {
            return (ErrorResponse) errorConverter.convert(response.errorBody());
        } catch (IOException e) {
            return new ErrorResponse();
        }
    }

    public static class ErrorResponse {
        public String message;
    }
}