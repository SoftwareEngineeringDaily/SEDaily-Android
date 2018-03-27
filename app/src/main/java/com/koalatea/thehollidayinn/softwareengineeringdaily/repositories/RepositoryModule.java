package com.koalatea.thehollidayinn.softwareengineeringdaily.repositories;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by keithholliday on 1/7/18.
 */

@Module
public class RepositoryModule {
    @Provides
    @Singleton
    UserRepository providesUserRepository(Application application) {
        return UserRepository.getInstance(application);
    }
}
