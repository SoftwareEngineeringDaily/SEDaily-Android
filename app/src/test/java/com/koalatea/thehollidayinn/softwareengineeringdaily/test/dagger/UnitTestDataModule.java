package com.koalatea.thehollidayinn.softwareengineeringdaily.test.dagger;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppScope;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.mapper.PostItemMapper;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.preference.AuthPreference;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.preference.AuthPreferenceImpl;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Created by Kurian on 26-Sep-17.
 */
@Module
public class UnitTestDataModule {

    @Provides
    @AppScope
    AuthPreference providesAuthPreference() {
        return mock(AuthPreference.class);
    }

    @Provides
    @AppScope
    PostItemMapper providesPostItemMapper() {
        return spy(new PostItemMapper());
    }
}
