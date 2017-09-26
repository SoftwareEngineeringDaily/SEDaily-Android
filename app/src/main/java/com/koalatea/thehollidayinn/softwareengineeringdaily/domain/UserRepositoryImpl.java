package com.koalatea.thehollidayinn.softwareengineeringdaily.domain;

import android.support.annotation.NonNull;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.preference.AuthPreference;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.api.AuthNetworkService;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.response.AuthResponse;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;

/**
 * Created by Kurian on 26-Sep-17.
 */

class UserRepositoryImpl implements UserRepository {

    private final AuthNetworkService api;
    private final AuthPreference preference;

    public UserRepositoryImpl(@NonNull AuthNetworkService api,
                              @NonNull AuthPreference preference) {
        this.api = api;
        this.preference = preference;
    }

    @Override
    public Single<Boolean> login(@NonNull String username, @NonNull String password) {
        return api.login(username, password)
                .map(new Function<AuthResponse, String>() {
                    @Override
                    public String apply(@NonNull AuthResponse authResponse) throws Exception {
                        return authResponse.token();
                    }
                })
                .map(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull String token) throws Exception {
                        preference.saveToken(token);
                        return preference.isLoggedIn();
                    }
                })
                .onErrorReturn(new Function<Throwable, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull Throwable throwable) throws Exception {
                        preference.clearToken();
                        return Boolean.FALSE;
                    }
                });
    }

    @Override
    public Single<Boolean> register(@NonNull String username, @NonNull String password) {
        return api.register(username, password)
                .map(new Function<AuthResponse, String>() {
                    @Override
                    public String apply(@NonNull AuthResponse authResponse) throws Exception {
                        return authResponse.token();
                    }
                })
                .map(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull String token) throws Exception {
                        preference.saveToken(token);
                        return preference.isLoggedIn();
                    }
                })
                .onErrorReturn(new Function<Throwable, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull Throwable throwable) throws Exception {
                        preference.clearToken();
                        return Boolean.FALSE;
                    }
                });
    }

    @Override
    public Completable signOut() {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                preference.clearToken();
            }
        });
    }
}
