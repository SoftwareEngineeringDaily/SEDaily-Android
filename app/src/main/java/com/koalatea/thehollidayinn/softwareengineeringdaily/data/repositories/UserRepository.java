package com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories;

/**
 * Created by krh12 on 6/21/2017.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by krh12 on 6/11/2017.
 */

public class UserRepository {
    private static UserRepository instance = null;

    private String TOKEN_KEY = "token-key";
    private String SUBSCRIBED_KEY = "subscribe-key";
    private Context context;
    private boolean isSubscribedToNotifications = false;
    private String token = "";
    private SharedPreferences preferences;

    protected UserRepository(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.token = preferences.getString(TOKEN_KEY, "");
        this.isSubscribedToNotifications = preferences.getBoolean(SUBSCRIBED_KEY, false);
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
        editor.putString(TOKEN_KEY, token);
        editor.apply();
    }

    public String getToken () {
        return this.token;
    }

    public void setSubscribed (Boolean isSubscribedToNotifications) {
        this.isSubscribedToNotifications = isSubscribedToNotifications;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SUBSCRIBED_KEY, isSubscribedToNotifications);
        editor.apply();
    }

    public Boolean getSubscribed () {
        return this.isSubscribedToNotifications;
    }
}
