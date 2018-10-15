package com.koalatea.sedaily.network

import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

const val BASE_URL: String = "https://software-enginnering-daily-api.herokuapp.com/api/";

class NetworkHelper {
    companion object {
        fun getApi(): SEDailyApi  {
            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .build()
                    .create(SEDailyApi::class.java)
        }
    }
}