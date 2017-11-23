package com.koalatea.thehollidayinn.softwareengineeringdaily.test.dagger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppScope;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.api.AuthNetworkService;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.api.EpisodePostNetworkService;

import com.koalatea.thehollidayinn.softwareengineeringdaily.network.response.ResponseAdapterFactory;
import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

/**
 * Created by Kurian on 25-Sep-17.
 */
@Module
public class UnitTestNetworkModule {

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
    EpisodePostNetworkService providesPostNetworkService() {
        return mock(EpisodePostNetworkService.class);
    }

    @Provides
    @AppScope
    AuthNetworkService providesAuthNetworkService() {
        return mock(AuthNetworkService.class);
    }
}
