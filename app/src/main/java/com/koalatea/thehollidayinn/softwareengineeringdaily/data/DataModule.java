package com.koalatea.thehollidayinn.softwareengineeringdaily.data;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.AppScope;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.mapper.PostItemMapper;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.preference.AuthPreference;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.preference.AuthPreferenceImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Kurian on 26-Sep-17.
 */
@Module
public class DataModule {

    @Provides
    @AppScope
    AuthPreference providesAuthPreference() {
        return new AuthPreferenceImpl();
    }

    @Provides
    @AppScope
    PostItemMapper providesPostItemMapper() {
        return new PostItemMapper();
    }
}
