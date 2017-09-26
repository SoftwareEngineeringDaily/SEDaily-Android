package com.koalatea.thehollidayinn.softwareengineeringdaily.domain;

import android.support.annotation.NonNull;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppScope;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.mapper.PostItemMapper;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.api.EpisodePostNetworkService;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Kurian on 26-Sep-17.
 */
@Module
public class DomainModule {

    @Provides
    @AppScope
    PostRepository providesPostRepository(@NonNull EpisodePostNetworkService service,
                                          @NonNull PostItemMapper mapper) {
        return new PostRepositoryImpl(service, mapper);
    }
}
