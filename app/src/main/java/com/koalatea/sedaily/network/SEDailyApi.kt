package com.koalatea.sedaily.network

import com.koalatea.sedaily.models.Episode
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface SEDailyApi {
    @GET("posts")
    fun getPosts(@QueryMap options: Map<String, String>): Observable<List<Episode>>
}