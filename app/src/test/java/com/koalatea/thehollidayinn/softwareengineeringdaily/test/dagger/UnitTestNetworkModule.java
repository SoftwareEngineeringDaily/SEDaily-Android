package com.koalatea.thehollidayinn.softwareengineeringdaily.test.dagger;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppScope;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.api.AuthNetworkService;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.api.EpisodePostNetworkService;

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
    EpisodePostNetworkService providesPostNetworkService() {
        return mock(EpisodePostNetworkService.class);
    }

    @Provides
    @AppScope
    AuthNetworkService providesAuthNetworkService() {
        return mock(AuthNetworkService.class);
    }
}
