package com.koalatea.thehollidayinn.softwareengineeringdaily.data.preference.base;

import android.content.SharedPreferences;

import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SDEApp;

/**
 * Base preference class to contain the common operations for preferences
 * Created by Kurian on 25-Sep-17.
 */
public abstract class BasePreferences implements PreferenceProvider {

    protected SharedPreferences getPrefs() {
        return SDEApp.component().sharedPreferences();
    }

    protected SharedPreferences.Editor getEditor() {
        return getPrefs().edit();
    }

    @Override
    public void clear() {
        getEditor().clear().apply();
    }
}
