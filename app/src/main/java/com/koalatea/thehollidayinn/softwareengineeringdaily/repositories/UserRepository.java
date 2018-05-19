package com.koalatea.thehollidayinn.softwareengineeringdaily.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/*
  Created by krh12 on 6/11/2017.
 */

public class UserRepository {
    private static UserRepository instance = null;

    private boolean isSubscribedToNotifications;
    private String token;
    private final SharedPreferences preferences;
    private Boolean hasPremium = false;

    private UserRepository(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.token = preferences.getString(RepoConstants.Companion.getTOKEN_KEY(), "");
        this.isSubscribedToNotifications = preferences.getBoolean(RepoConstants.Companion.getSUBSCRIBED_KEY(), false);
    }

    public static UserRepository getInstance(Context context) {
        if(instance == null) {
            instance = new UserRepository(context);
        }
        return instance;
    }

    public void setToken (String token) {
        this.token = token;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(RepoConstants.Companion.getTOKEN_KEY(), token);
        editor.apply();
    }

    public String getToken () {
        return this.token;
    }

    public void setSubscribed (Boolean isSubscribedToNotifications) {
        this.isSubscribedToNotifications = isSubscribedToNotifications;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(RepoConstants.Companion.getSUBSCRIBED_KEY(), isSubscribedToNotifications);
        editor.apply();
    }

    public Boolean getSubscribed () {
        return this.isSubscribedToNotifications;
    }

    public Boolean getHasPremium() {
        return hasPremium;
    }

    public void setHasPremium(Boolean hasPremium) {
        this.hasPremium = hasPremium;
    }
}
