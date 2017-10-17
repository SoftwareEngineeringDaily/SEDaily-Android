package com.koalatea.thehollidayinn.softwareengineeringdaily.test.dagger;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppScope;
import com.koalatea.thehollidayinn.softwareengineeringdaily.domain.PostRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.domain.UserRepository;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

/**
 * Created by Kurian on 26-Sep-17.
 */
@Module
public class UnitTestDomainModule {

    @Provides
    @AppScope
    PostRepository providesPostRepository() {
        return mock(PostRepository.class);
    }

    @Provides
    @AppScope
    UserRepository providesUserRepository() {
        return mock(UserRepository.class);
    }
}
