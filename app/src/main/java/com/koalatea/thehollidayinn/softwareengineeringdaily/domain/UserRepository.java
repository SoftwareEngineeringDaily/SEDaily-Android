package com.koalatea.thehollidayinn.softwareengineeringdaily.domain;

import android.support.annotation.NonNull;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Kurian on 26-Sep-17.
 */

public interface UserRepository {

    /**
     * Log in with an existing account
     * @param username
     * @param password
     * @return true if log in is successful
     */
    Single<Boolean> login(@NonNull String username, @NonNull String password);

    /**
     * Register a new account
     * @param username
     * @param password
     * @return true if registration is successful
     */
    Single<Boolean> register(@NonNull String username, @NonNull String password);

    /**
     * Sign out of the current account
     * @return
     */
    Completable signOut();

    /**
     * Check if user is currently logged in
     * @return true if there's an active account
     */
    Single<Boolean> isLoggedIn();
}
