package com.koalatea.thehollidayinn.softwareengineeringdaily.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.koalatea.thehollidayinn.softwareengineeringdaily.analytics.AnalyticsFacade;
import com.koalatea.thehollidayinn.softwareengineeringdaily.analytics.AnalyticsModule;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.DataModule;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.mapper.PostItemMapper;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.preference.AuthPreference;
import com.koalatea.thehollidayinn.softwareengineeringdaily.domain.DomainModule;
import com.koalatea.thehollidayinn.softwareengineeringdaily.domain.PostRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.domain.UserRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.NetworkModule;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.api.AuthNetworkService;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.api.EpisodePostNetworkService;
import com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.PresenterCache;
import com.koalatea.thehollidayinn.softwareengineeringdaily.utils.LocalTextUtils;

import dagger.Component;

/**
 * Created by Kurian on 25-Sep-17.
 */
@AppScope
@Component(modules = {AppModule.class, NetworkModule.class, DataModule.class, DomainModule.class,
    AnalyticsModule.class})
public interface AppComponent {
    Context context();
    SharedPreferences sharedPreferences();
    LocalTextUtils textUtils();
    AuthPreference authPreference();
    PostItemMapper mapper();
    EpisodePostNetworkService episodePostNetworkService();
    AuthNetworkService authNetworkService();
    PostRepository postRepository();
    UserRepository userRepository();
    PresenterCache presenterCache();
    Gson gson();
    AnalyticsFacade analyticsFacade();
}
