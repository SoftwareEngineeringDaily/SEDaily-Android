package com.koalatea.thehollidayinn.softwareengineeringdaily.data.preference;

import android.support.annotation.VisibleForTesting;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SDEApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.preference.base.BasePreferences;

import timber.log.Timber;

/**
 * Created by Kurian on 25-Sep-17.
 */
public class AuthPreferenceImpl extends BasePreferences implements AuthPreference {

    @VisibleForTesting
    static final String AUTH_TOKEN = "auth_token";

    @Override
    public void saveToken(String token) {
        Timber.tag(AuthPreferenceImpl.class.getCanonicalName()).d("Saving token: %1$s", token);
        getEditor().putString(AUTH_TOKEN, token).apply();
    }

    @Override
    public String getToken() {
        return getPrefs().getString(AUTH_TOKEN, TOKEN_DEFAULT);
    }

    @Override
    public boolean isLoggedIn() {
        return !SDEApp.component().textUtils().isEmpty(getToken());
    }

    @Override
    public void clearToken() {
        getEditor().remove(AUTH_TOKEN).apply();
    }
}
